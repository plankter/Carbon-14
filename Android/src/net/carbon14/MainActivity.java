package net.carbon14;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button buttonScan = (Button) findViewById(R.id.ButtonScan);
        buttonScan.setOnClickListener(mScanListener);
    }
    
	// Create an anonymous implementation of OnClickListener
    private OnClickListener mScanListener = new OnClickListener() {
        public void onClick(View v) {
        	// do something when the button is clicked
        	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            startActivityForResult(intent, 0);
        }
    };
}