
package net.carbon14.android;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TabHost;

public class DetailsActivity extends TabActivity {
	private final static int INPUT_REQUEST_CODE = 1;
	private final static int PREFERENCES_REQUEST_CODE = 2;
	
	private String mVersionName;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.details);

		TabHost tabHost = getTabHost();

		if (ProviderManager.carbonEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_carbon").setIndicator("Carbon").setContent(R.id.carbonLayout));
		}

		if (ProviderManager.upcEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_upc").setIndicator("UPC").setContent(R.id.upcLayout));
		}

		if (ProviderManager.ratingEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_rating").setIndicator("Rating").setContent(R.id.ratingLayout));
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Intent intent = getIntent();
		if (intent == null) return;
		String barcode = intent.getStringExtra("barcode");
		submitBarcode(barcode);
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
			startActivityForResult(intent, PREFERENCES_REQUEST_CODE);
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
			
		}
	}

	private void submitBarcode(String barcode) {
		if (barcode == null) return;
		
		if (ProviderManager.upcEnabled) {
			Provider provider = ProviderManager.providers.get("UPC Database");
			if (provider != null) {
				WebView webView = (WebView) findViewById(R.id.upcWebView);
				webView.setVerticalScrollbarOverlay(true);
				String url = provider.getDetailsUrl() + "?barcode=" + barcode;
				webView.loadUrl(url);
			}
		}

		if (ProviderManager.ratingEnabled) {
			Provider provider = ProviderManager.providers.get("Rating");
			if (provider != null) {
				WebView webView = (WebView) findViewById(R.id.ratingWebView);
				webView.setVerticalScrollbarOverlay(true);
				String url = provider.getDetailsUrl() + "?barcode=" + barcode;
				webView.loadUrl(url);
			}
		}

		if (ProviderManager.carbonEnabled) {
			Provider provider = ProviderManager.providers.get("Environment");
			if (provider != null) {
				WebView webView = (WebView) findViewById(R.id.carbonWebView);
				webView.setVerticalScrollbarOverlay(true);
				String url = provider.getDetailsUrl() + "?barcode=" + barcode;
				webView.loadUrl(url);
			}
		}
	}

	private final DialogInterface.OnClickListener mAboutListener = new DialogInterface.OnClickListener() {
		public void onClick(android.content.DialogInterface dialogInterface, int i) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.zxing_url)));
			startActivity(intent);
		}
	};
}