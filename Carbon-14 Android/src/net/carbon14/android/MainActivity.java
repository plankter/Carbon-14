/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * Modified by Anton Rau.
 * 
 * 
 */

package net.carbon14.android;

import java.io.IOException;

import net.carbon14.android.result.ResultButtonListener;
import net.carbon14.android.result.ResultHandler;
import net.carbon14.android.result.ResultHandlerFactory;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
	private static final String TAG = "MainActivity";

	private static final int MAX_RESULT_IMAGE_SIZE = 150;
	private static final float BEEP_VOLUME = 0.15f;
	private static final long VIBRATE_DURATION = 200;

	private static final String PACKAGE_NAME = "net.carbon14.android";

	private enum Source {
		NATIVE_APP_INTENT, PRODUCT_SEARCH_LINK, ZXING_LINK, NONE
	}

	public CaptureActivityHandler mHandler;

	public static String activeBarcode;

	private ViewfinderView mViewfinderView;
	private View mStatusView;
	private View mResultView;
	private MediaPlayer mMediaPlayer;
	private Result mLastResult;
	private boolean mHasSurface;
	private boolean mPlayBeep;
	private boolean mVibrate;
	private boolean mCopyToClipboard;
	private Source mSource = Source.NONE;
	private String mDecodeMode = Intents.Scan.PRODUCT_MODE;
	private String mVersionName;

	private final OnCompletionListener mBeepListener = new BeepListener();

	private final static int INPUT_REQUEST_CODE = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main);

		CameraManager.init(getApplication());
		mViewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		mResultView = findViewById(R.id.result_view);
		mStatusView = findViewById(R.id.status_view);
		mHandler = null;
		mLastResult = null;
		mHasSurface = false;

		showHelpOnFirstLaunch();

		ConnectivityManager connectivityManager = (ConnectivityManager) this.getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		ProviderManager providers = new ProviderManager(connectivityManager);
		ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "", "Loading providers. Please wait...", true);
		Boolean ready = providers.reload();
		dialog.dismiss();
		if (!ready)
			Toast.makeText(this, "Network is not available.", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onResume() {
		super.onResume();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (mHasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		resetStatusView();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		ProviderManager.carbonEnabled = prefs.getBoolean(PreferencesActivity.PROVIDER_CARBON, true);
		ProviderManager.energyEnabled = prefs.getBoolean(PreferencesActivity.PROVIDER_ENERGY, true);
		ProviderManager.aprioriEnabled = prefs.getBoolean(PreferencesActivity.PROVIDER_APRIORI, true);
		ProviderManager.upcEnabled = prefs.getBoolean(PreferencesActivity.PROVIDER_UPC, true);

		mPlayBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, true);
		mVibrate = prefs.getBoolean(PreferencesActivity.KEY_VIBRATE, false);
		mCopyToClipboard = prefs.getBoolean(PreferencesActivity.KEY_COPY_TO_CLIPBOARD, true);
		initBeepSound();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.inputMenuItem: {
				Intent intent = new Intent(this, ManualInputActivity.class);
				startActivityForResult(intent, INPUT_REQUEST_CODE);
				break;
			}
			case R.id.preferencesMenuItem: {
				Intent intent = new Intent(this, PreferencesActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.helpMenuItem: {
				Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.aboutMenuItem: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.title_about) + mVersionName);
				builder.setMessage(getString(R.string.msg_about) + "\n\n" + getString(R.string.zxing_url));
				builder.setIcon(R.drawable.zxing_icon);
				builder.setPositiveButton(R.string.button_open_browser, mAboutListener);
				builder.setNegativeButton(R.string.button_cancel, null);
				builder.show();
				break;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case INPUT_REQUEST_CODE: {
				if (resultCode == RESULT_OK) {
					String barcode = data.getStringExtra("BARCODE");
					// Handle successful scan
					showDetails(barcode);
				}
				break;
			}
		}
	}
	
	public void showDetails(String barcode)
	{
		if (barcode == null) return;
		
		Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
		intent.putExtra("barcode", barcode);
		startActivity(intent);
	}

	private void submitBarcode(String barcode) {
		if (barcode == null)
			return;

		if (ProviderManager.carbonEnabled) {
			Provider provider = ProviderManager.providers.get("Carbon");
			if (provider != null) {
				WebView widgetWebView = (WebView) findViewById(R.id.carbonWidgetWebView);
				widgetWebView.setVerticalScrollbarOverlay(true);
				String url = provider.getWidgetUrl() + "?barcode=" + barcode;
				widgetWebView.loadUrl(url);
			}
		}
		

		if (ProviderManager.energyEnabled) {
			Provider provider = ProviderManager.providers.get("Energy");
			if (provider != null) {
				WebView widgetWebView = (WebView) findViewById(R.id.energyWidgetWebView);
				widgetWebView.setVerticalScrollbarOverlay(true);
				String url = provider.getWidgetUrl() + "?barcode=" + barcode;
				widgetWebView.loadUrl(url);
			}
		}
		
		if (ProviderManager.upcEnabled) {
			Provider provider = ProviderManager.providers.get("UPC");
			if (provider != null) {
				WebView widgetWebView = (WebView) findViewById(R.id.upcWidgetWebView);
				widgetWebView.setVerticalScrollbarOverlay(true);
				String url = provider.getWidgetUrl() + "?barcode=" + barcode;
				widgetWebView.loadUrl(url);
			}
		}

		if (ProviderManager.aprioriEnabled) {
			Provider provider = ProviderManager.providers.get("APriori");
			if (provider != null) {
				WebView widgetWebView = (WebView) findViewById(R.id.aprioriWidgetWebView);
				widgetWebView.setVerticalScrollbarOverlay(true);
				String url = provider.getWidgetUrl() + "?barcode=" + barcode;
				widgetWebView.loadUrl(url);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mHandler != null) {
			mHandler.quitSynchronously();
			mHandler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mSource == Source.NATIVE_APP_INTENT) {
				setResult(RESULT_CANCELED);
				finish();
				return true;
			} else if ((mSource == Source.NONE || mSource == Source.ZXING_LINK) && mLastResult != null) {
				resetStatusView();
				mHandler.sendEmptyMessage(R.id.restart_preview);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// Don't display the share menu item if the result overlay is showing.
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// menu.findItem(SHARE_ID).setVisible(mLastResult == null);
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		// Do nothing, this is to prevent the activity from being restarted when
		// the keyboard opens.
		super.onConfigurationChanged(config);
	}

	private final DialogInterface.OnClickListener mAboutListener = new DialogInterface.OnClickListener() {
		public void onClick(android.content.DialogInterface dialogInterface, int i) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.zxing_url)));
			startActivity(intent);
		}
	};

	public void surfaceCreated(SurfaceHolder holder) {
		if (!mHasSurface) {
			mHasSurface = true;
			initCamera(holder);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mHasSurface = false;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode) {
		mLastResult = rawResult;
		playBeepSoundAndVibrate();
		drawResultPoints(barcode, rawResult);

		handleDecodeInternally(rawResult, barcode);
	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of
	 * the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_image_border));
			paint.setStrokeWidth(3);
			paint.setStyle(Paint.Style.STROKE);
			Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
			canvas.drawRect(border, paint);

			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4);
				canvas.drawLine(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), paint);
			} else {
				paint.setStrokeWidth(10);
				for (int x = 0; x < points.length; x++) {
					canvas.drawPoint(points[x].getX(), points[x].getY(), paint);
				}
			}
		}
	}

	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult, Bitmap barcode) {
		mStatusView.setVisibility(View.GONE);
		mViewfinderView.setVisibility(View.GONE);
		mResultView.setVisibility(View.VISIBLE);

		ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);
		barcodeImageView.setMaxWidth(MAX_RESULT_IMAGE_SIZE);
		barcodeImageView.setMaxHeight(MAX_RESULT_IMAGE_SIZE);
		barcodeImageView.setImageBitmap(barcode);

		TextView formatTextView = (TextView) findViewById(R.id.format_text_view);
		formatTextView.setText(getString(R.string.msg_default_format) + ": " + rawResult.getBarcodeFormat().toString());

		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
		TextView typeTextView = (TextView) findViewById(R.id.type_text_view);
		typeTextView.setText(getString(R.string.msg_default_type) + ": " + resultHandler.getType().toString());

		TextView contentsTextView = (TextView) findViewById(R.id.contents_text_view);
		String displayContents = resultHandler.getDisplayContents().toString();
		contentsTextView.setText(getString(R.string.msg_default_code) + ": " + displayContents);

		int buttonCount = resultHandler.getButtonCount();
		ViewGroup buttonView = (ViewGroup) findViewById(R.id.result_button_view);
		buttonView.requestFocus();
		for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
			Button button = (Button) buttonView.getChildAt(x);
			if (x < buttonCount) {
				button.setVisibility(View.VISIBLE);
				button.setText(resultHandler.getButtonText(x));
				button.setOnClickListener(new ResultButtonListener(resultHandler, x));
			} else {
				button.setVisibility(View.GONE);
			}
		}

		if (mCopyToClipboard) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(displayContents);
		}

		submitBarcode(displayContents);
	}

	/**
	 * We want the help screen to be shown automatically the first time a new
	 * version of the app is run. The easiest way to do this is to check
	 * android:versionCode from the manifest, and compare it to a value stored
	 * as a preference.
	 */
	private void showHelpOnFirstLaunch() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			int currentVersion = info.versionCode;
			// Since we're paying to talk to the PackageManager anyway, it makes
			// sense to cache the app
			// version name here for display in the about box later.
			this.mVersionName = info.versionName;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int lastVersion = prefs.getInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, 0);
			if (currentVersion > lastVersion) {
				prefs.edit().putInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, currentVersion).commit();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(this, HelpActivity.class.getName());
				startActivity(intent);
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, e);
		}
	}

	/**
	 * Creates the beep MediaPlayer in advance so that the sound can be
	 * triggered with the least latency possible.
	 */
	private void initBeepSound() {
		if (mPlayBeep && mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
			mMediaPlayer.setOnCompletionListener(mBeepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mMediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mMediaPlayer.prepare();
			} catch (IOException e) {
				mMediaPlayer = null;
			}
		}
	}

	private void playBeepSoundAndVibrate() {
		if (mPlayBeep && mMediaPlayer != null) {
			mMediaPlayer.start();
		}
		if (mVibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mHandler == null) {
			boolean beginScanning = mLastResult == null;
			mHandler = new CaptureActivityHandler(this, mDecodeMode, beginScanning);
		}
	}

	private void resetStatusView() {
		mResultView.setVisibility(View.GONE);
		mStatusView.setVisibility(View.VISIBLE);
		mStatusView.setBackgroundColor(getResources().getColor(R.color.status_view));
		mViewfinderView.setVisibility(View.VISIBLE);

		TextView textView = (TextView) findViewById(R.id.status_text_view);
		textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		textView.setTextSize(14.0f);
		textView.setText(R.string.msg_default_status);
		mLastResult = null;
	}

	public void drawViewfinder() {
		mViewfinderView.drawViewfinder();
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private static class BeepListener implements OnCompletionListener {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	}
}