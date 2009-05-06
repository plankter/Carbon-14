package net.carbon14.android;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ProviderManager {
	public static boolean carbonEnabled;
	public static boolean upcEnabled;
	public static boolean ratingEnabled;
	
	public static HashMap<String, Provider> providers;
	public final static String PROVIDERS_URL = "http://carbon-14.appspot.com/services/providers/get";

	private ConnectivityManager connectivityManager;
	
	public ProviderManager(ConnectivityManager connectivityManager) {
		// TODO Auto-generated constructor stub
		this.connectivityManager = connectivityManager;
	}

	public boolean reload() {
		if (!connectivityManager.getActiveNetworkInfo().isAvailable())
			return false;
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(PROVIDERS_URL);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		try {
			String responseBody = httpClient.execute(request, responseHandler);

			XStream xstream = new XStream(new DomDriver());
			xstream.alias("provider", Provider.class);
			xstream.alias("providers", Provider[].class);

			Provider[] providersArray = (Provider[]) xstream.fromXML(responseBody);
			providers = new HashMap<String, Provider>(providersArray.length);
			for (Provider provider : providersArray) {
				providers.put(provider.getName(), provider);
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		httpClient.getConnectionManager().shutdown();
		return true;
	}
}
