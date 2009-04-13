package net.carbon14.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class ProfileActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);

		Intent intent = getIntent();
		String action = intent.getAction();
		if (intent != null && action != null && action.equals("net.carbon14.PROFILE")) {
			WebView webView = (WebView) findViewById(R.id.WebViewProfile);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.loadUrl(getString(R.string.profile_url));
		}
	}
}
