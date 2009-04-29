/*
 * Copyright (C) 2008 ZXing authors
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

package net.carbon14.android.result;

import net.carbon14.android.LocaleManager;
import net.carbon14.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

public abstract class ResultHandler {
	public static final int MAX_BUTTON_COUNT = 4;

	protected final ParsedResult mResult;
	private final Activity mActivity;

	protected ResultHandler(Activity activity, ParsedResult result) {
		mResult = result;
		mActivity = activity;
	}

	/**
	 * Indicates how many buttons the derived class wants shown.
	 * 
	 * @return The integer button count.
	 */
	public abstract int getButtonCount();

	/**
	 * The text of the nth action button.
	 * 
	 * @param index
	 *            From 0 to getButtonCount() - 1
	 * @return The button text as a resource ID
	 */
	public abstract int getButtonText(int index);

	/**
	 * Execute the action which corresponds to the nth button.
	 * 
	 * @param index
	 *            The button that was clicked.
	 */
	public abstract void handleButtonPress(int index);

	/**
	 * Create a possibly styled string for the contents of the current barcode.
	 * 
	 * @return The text to be displayed.
	 */
	public CharSequence getDisplayContents() {
		String contents = mResult.getDisplayResult();
		return contents.replace("\r", "");
	}

	/**
	 * A string describing the kind of barcode that was found, e.g.
	 * "Found contact info".
	 * 
	 * @return The resource ID of the string.
	 */
	public abstract int getDisplayTitle();

	/**
	 * A convenience method to get the parsed type. Should not be overridden.
	 * 
	 * @return The parsed type, e.g. URI or ISBN
	 */
	public final ParsedResultType getType() {
		return mResult.getType();
	}

	public final void openMap(String geoURI) {
		launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(geoURI)));
	}

	/**
	 * Do a geo search using the address as the query.
	 * 
	 * @param address
	 *            The address to find
	 * @param title
	 *            An optional title, e.g. the name of the business at this
	 *            address
	 */
	public final void searchMap(String address, String title) {
		String query = address;
		if (title != null && title.length() > 0) {
			query = query + " (" + title + ')';
		}
		launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="
				+ Uri.encode(query))));
	}

	public final void getDirections(double latitude, double longitude) {
		launchIntent(new Intent(Intent.ACTION_VIEW, Uri
				.parse("http://maps.google." + LocaleManager.getCountryTLD()
						+ "/maps?f=d&daddr=" + latitude + ',' + longitude)));
	}

	public final void openProductSearch(String upc) {
		Uri uri = Uri.parse("http://www.google."
				+ LocaleManager.getCountryTLD() + "/products?q=" + upc);
		launchIntent(new Intent(Intent.ACTION_VIEW, uri));
	}

	public final void openURL(String url) {
		launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	}

	public final void webSearch(String query) {
		Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
		intent.putExtra("query", query);
		launchIntent(intent);
	}

	private void launchIntent(Intent intent) {
		if (intent != null) {
			try {
				mActivity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle(mActivity.getString(R.string.app_name));
				builder.setMessage(mActivity
						.getString(R.string.msg_intent_failed));
				builder.setPositiveButton(R.string.button_ok, null);
				builder.show();
			}
		}
	}

}
