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

import net.carbon14.android.MainActivity;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

public final class ResultHandlerFactory {

	private ResultHandlerFactory() {
	}

	public static ResultHandler makeResultHandler(MainActivity activity, Result rawResult) {
		ParsedResult result = parseResult(rawResult);

		return new BarcodeResultHandler(activity, result);
	}

	private static ParsedResult parseResult(Result rawResult) {
		ParsedResult result = ResultParser.parseResult(rawResult);
		return result;
	}

}
