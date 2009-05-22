/*
 * Copyright (C) 2009 Anton Rau
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
 */

package net.carbon14.android;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
		
		if (ProviderManager.energyEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_energy").setIndicator("Energy").setContent(R.id.energyLayout));
		}
		
		if (ProviderManager.aprioriEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_apriori").setIndicator("APriori").setContent(R.id.aprioriLayout));
		}

		if (ProviderManager.upcEnabled) {
			tabHost.addTab(tabHost.newTabSpec("tab_upc").setIndicator("UPC").setContent(R.id.upcLayout));
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
		
		if (ProviderManager.carbonEnabled) {
			Provider provider = ProviderManager.providers.get("Carbon");
			if (provider != null) {
				WebView webView = (WebView) findViewById(R.id.carbonWebView);
				webView.setWebViewClient(new WebViewClient() {  
				    /* shouldOverrideUrlLoading() will be called every time the user clicks a link */  
				    @Override  
				    public boolean shouldOverrideUrlLoading(WebView view, String url)  
				    { 
				        view.loadUrl(url);
				        return true;  
				    }  
				});
				webView.setVerticalScrollbarOverlay(true);
				webView.getSettings().setJavaScriptEnabled(true);
				String url = provider.getDetailsUrl() + "?barcode=" + barcode;
				webView.loadUrl(url);
			}
		}
		
		if (ProviderManager.energyEnabled) {
			Provider provider = ProviderManager.providers.get("Energy");
			if (provider != null) {
				WebView webView = (WebView) findViewById(R.id.energyWebView);
				webView.setVerticalScrollbarOverlay(true);
				String url = provider.getDetailsUrl() + "?barcode=" + barcode;
				webView.loadUrl(url);
			}
		}
		
		if (ProviderManager.aprioriEnabled) {
			Provider provider = ProviderManager.providers.get("APriori");
			if (provider != null) {
				WebView webView = (WebView) findViewById(R.id.aprioriWebView);
				webView.setVerticalScrollbarOverlay(true);
				String url = provider.getDetailsUrl() + "?barcode=" + barcode;
				webView.loadUrl(url);
			}
		}
		
		if (ProviderManager.upcEnabled) {
			Provider provider = ProviderManager.providers.get("UPC");
			if (provider != null) {
				WebView webView = (WebView) findViewById(R.id.upcWebView);
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