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
	public static boolean energyEnabled;
	public static boolean aprioriEnabled;
	public static boolean upcEnabled;
	
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
			httpClient.getConnectionManager().shutdown();

			XStream xstream = new XStream(new DomDriver());
			xstream.alias("provider", Provider.class);
			xstream.alias("providers", Provider[].class);

			Provider[] providersArray = (Provider[]) xstream.fromXML(responseBody);
			providers = new HashMap<String, Provider>(providersArray.length);
			for (Provider provider : providersArray) {
				providers.put(provider.getName(), provider);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (providers != null);
	}
}
