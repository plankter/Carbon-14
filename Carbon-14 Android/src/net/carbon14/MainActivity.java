package net.carbon14;

import com.google.zxing.client.android.PreferencesActivity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final static int SCAN_REQUEST_CODE = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button buttonScan = (Button) findViewById(R.id.ButtonScan);
        buttonScan.setOnClickListener(scanListener);
        
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
        case R.id.profileMenuItem:
        	showUserProfile();
            return true;
        case R.id.productMenuItem:
        	showProductProfile();
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
    
    private void productRecognized(String contents, String format)
    {
    	Spinner spinner = (Spinner) findViewById(R.id.SpinnerBarcodeFormat);
    	
    	if (format != null)
    	{
	    	ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
	    	spinner.setSelection(adapter.getPosition(format));
    	}
    	
    	if (contents != null)
    	{
    		EditText editText = (EditText) findViewById(R.id.EditTextCode);
    		editText.setText(contents);
    	}
    }
    
    private void showUserProfile()
    {
    	Intent intent = new Intent("net.carbon14.PROFILE");
        startActivity(intent);
    }
    
    private void showProductProfile()
    {
    	Intent intent = new Intent("net.carbon14.PRODUCT");
        startActivity(intent);
    }
    
    private void startScanning()
    {
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
    	intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
        startActivityForResult(intent, SCAN_REQUEST_CODE);
    }
}