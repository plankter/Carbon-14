package net.carbon14;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;

public class ProductActivity extends Activity {
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product);
        
        Intent intent = getIntent();
        String action = intent.getAction();
        if (intent != null && action != null && action.equals("net.carbon14.PRODUCT"))
        {        
	        ImageView imageView = (ImageView) findViewById(R.id.ImageViewBenchmark);
	        try {
				imageView.setImageBitmap(BitmapFactory.decodeStream(new URL("http://chart.apis.google.com/chart?chs=250x100&cht=gom&chd=t:70&chl=70%&chf=a,s,EFEFEFF0&chtt=Benchmark").openStream()));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }
    }
}
