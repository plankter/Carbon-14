package net.carbon14.android;

import java.io.IOException;
import java.util.Dictionary;

import net.carbon14.core.Provider;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class ProviderManager {
	public static Dictionary<String, Provider> Providers;
	public final static String PROVIDERS_URL = "http://10.0.2.2:8080/providers";
	
	public void reload()
	{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(PROVIDERS_URL);
		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
			String responseBody = httpClient.execute(request, responseHandler);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        httpClient.getConnectionManager().shutdown();
	}
}
