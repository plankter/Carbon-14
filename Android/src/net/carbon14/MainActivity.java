package net.carbon14;

import com.google.zxing.client.android.PreferencesActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final static int SCAN_REQUEST_CODE = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button buttonScan = (Button) findViewById(R.id.ButtonScan);
        buttonScan.setOnClickListener(mScanListener);
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
        	StartScanning();
            return true;
        case R.id.profileMenuItem:
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
    private OnClickListener mScanListener = new OnClickListener() {
        public void onClick(View v) {
        	StartScanning();
        }
    };
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
                ProductRegistered(contents, format);
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            	Toast.makeText(this, "Scanning Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void ProductRegistered(String contents, String format)
    {
    	
    }
    
    private void StartScanning()
    {
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
    	intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
        startActivityForResult(intent, SCAN_REQUEST_CODE);
    }
}