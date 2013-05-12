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

package net.carbon14.android.result;

import net.carbon14.android.MainActivity;
import net.carbon14.android.R;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ProductParsedResult;

public final class BarcodeResultHandler extends ResultHandler {

	private static final int[] mButtons = { R.string.button_details,
		R.string.button_product_search,
		R.string.button_web_search,
	};

	public BarcodeResultHandler(MainActivity activity, ParsedResult result) {
		super(activity, result);
	}

	@Override
	public int getButtonCount() {
		return mButtons.length;
	}

	@Override
	public int getButtonText(int index) {
		return mButtons[index];
	}

	@Override
	public void handleButtonPress(int index) {
		switch (index) {
			case 0:
				showDetails(mResult.getDisplayResult());
				break;
				
			case 1:
				openProductSearch(mResult.getDisplayResult());
				break;
				
			case 2:
				webSearch(mResult.getDisplayResult());
				break;
		}
	}

	@Override
	public int getDisplayTitle() {
		return R.string.result_text;
	}
}
