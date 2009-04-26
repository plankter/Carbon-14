package net.carbon14.android;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends TabActivity {
	private final static int SCAN_REQUEST_CODE = 0;
	
	private Boolean carbonEnabled;
	private Boolean upcEnabled;
	private Boolean ratingEnabled;
	
	private TabHost.TabSpec tabInput;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ProviderManager providers = new ProviderManager();
		providers.reload();
	}

	@Override
	protected void onResume() {
		super.onResume();

		setContentView(R.layout.main);

		TabHost tabHost = getTabHost();

		tabInput = tabHost.newTabSpec("tab_input").setIndicator("Input").setContent(R.id.inputLayout);
		tabHost.addTab(tabInput);
		setDefaultTab("tab_input");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		carbonEnabled = prefs.getBoolean(PreferencesActivity.PROVIDER_CARBON, false);
		if (carbonEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_carbon").setIndicator("Carbon").setContent(R.id.carbonLayout));
		}
		
		upcEnabled = prefs.getBoolean(PreferencesActivity.PROVIDER_UPC, false);
		if (upcEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_upc").setIndicator("UPC").setContent(R.id.upcLayout));
		}

		ratingEnabled = prefs.getBoolean(PreferencesActivity.PROVIDER_RATING, false);
		if (ratingEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_rating").setIndicator("Rating").setContent(R.id.ratingLayout));
		}

		Button buttonScan = (Button) findViewById(R.id.ButtonScan);
		buttonScan.setOnClickListener(scanListener);
		
		Button buttonSubmit = (Button) findViewById(R.id.ButtonSubmit);
		buttonSubmit.setOnClickListener(submitListener);

		Spinner s = (Spinner) findViewById(R.id.SpinnerBarcodeFormat);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.barcode_format, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scanMenuItem:
			startScanning();
			return true;
		case R.id.settingsMenuItem:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(this, PreferencesActivity.class.getName());
			startActivity(intent);
			return true;
		}
		return false;
	}

	// Create an anonymous implementation of OnClickListener
	private OnClickListener scanListener = new OnClickListener() {
		public void onClick(View v) {
			startScanning();
		}
	};
	
	private OnClickListener submitListener = new OnClickListener() {
		public void onClick(View v) {
			submitBarcode();
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == SCAN_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				productRecognized(contents, format);
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
				Toast.makeText(this, "Scanning Cancelled", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void productRecognized(String contents, String format) {
		Spinner spinner = (Spinner) findViewById(R.id.SpinnerBarcodeFormat);

		if (format != null) {
			ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
			spinner.setSelection(adapter.getPosition(format));
		}

		if (contents != null) {
			EditText editText = (EditText) findViewById(R.id.EditTextCode);
			editText.setText(contents);
		}
	}

	private void showUserProfile() {
		Intent intent = new Intent("PROFILE");
		startActivity(intent);
	}

	private void showProductProfile() {
		Intent intent = new Intent("PRODUCT");
		startActivity(intent);
	}

	private void startScanning() {
		Intent intent = new Intent("SCAN");
		intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
		startActivityForResult(intent, SCAN_REQUEST_CODE);
	}
	
	private void submitBarcode() {
		EditText editText = (EditText) findViewById(R.id.EditTextCode);
		if (upcEnabled) {
			Provider provider = ProviderManager.providers.get("UPC Database");
			if (provider != null)
			{
				WebView webView = (WebView) findViewById(R.id.upcWebView);
				webView.setVerticalScrollbarOverlay(true);
				String url = provider.getDetailsUrl() + "?barcode=" + editText.getText();
				webView.loadUrl(url);
			}
		}
		
		if (ratingEnabled) {
			Provider provider = ProviderManager.providers.get("Rating");
			if (provider != null)
			{
				WebView webView = (WebView) findViewById(R.id.ratingWebView);
				webView.setVerticalScrollbarOverlay(true);
				String url = provider.getDetailsUrl() + "?barcode=" + editText.getText();
				webView.loadUrl(url);
			}
		}
		
		if (carbonEnabled) {
			Provider provider = ProviderManager.providers.get("Environment");
			if (provider != null)
			{
				WebView webView = (WebView) findViewById(R.id.carbonWebView);
				webView.setVerticalScrollbarOverlay(true);
				String url = provider.getDetailsUrl() + "?barcode=" + editText.getText();
				webView.loadUrl(url);
			}
		}
	}
}