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
 * 
 * 
 * Modified by Anton Rau.
 * 
 * 
 */

package net.carbon14.android;

public final class Intents {

	private Intents() {
	}

	public static final class Scan {
		/**
		 * Send this intent to open the Barcodes app in scanning mode, find a
		 * barcode, and return the results.
		 */
		public static final String ACTION = "net.carbon14.android.SCAN";

		/**
		 * By default, sending Scan.ACTION will decode all barcodes that we
		 * understand. However it may be useful to limit scanning to certain
		 * formats. Use Intent.putExtra(MODE, value) with one of the values
		 * below (optional).
		 */
		public static final String MODE = "SCAN_MODE";

		/**
		 * Decode only UPC and EAN barcodes. This is the right choice for
		 * shopping apps which get prices, reviews, etc. for products.
		 */
		public static final String PRODUCT_MODE = "PRODUCT_MODE";

		/**
		 * Decode only 1D barcodes (currently UPC, EAN, Code 39, and Code 128).
		 */
		public static final String ONE_D_MODE = "ONE_D_MODE";

		/**
		 * Decode only QR codes.
		 */
		public static final String QR_CODE_MODE = "QR_CODE_MODE";

		/**
		 * If a barcode is found, Barcodes returns RESULT_OK to
		 * onActivityResult() of the app which requested the scan via
		 * startSubActivity(). The barcodes contents can be retrieved with
		 * intent.getStringExtra(RESULT). If the user presses Back, the result
		 * code will be RESULT_CANCELED.
		 */
		public static final String RESULT = "SCAN_RESULT";

		/**
		 * Call intent.getStringExtra(RESULT_FORMAT) to determine which barcode
		 * format was found. See Contents.Format for possible values.
		 */
		public static final String RESULT_FORMAT = "SCAN_RESULT_FORMAT";

		private Scan() {
		}
	}

	public static final class Encode {
		/**
		 * Send this intent to encode a piece of data as a QR code and display
		 * it full screen, so that another person can scan the barcode from your
		 * screen.
		 */
		public static final String ACTION = "net.carbon14.android.ENCODE";

		/**
		 * The data to encode. Use Intent.putExtra(DATA, data) where data is
		 * either a String or a Bundle, depending on the type specified. See
		 * Contents for details.
		 */
		public static final String DATA = "ENCODE_DATA";

		/**
		 * The type of data being supplied. Use Intent.putExtra(TYPE, type) with
		 * one of Contents.Type.
		 */
		public static final String TYPE = "ENCODE_TYPE";

		private Encode() {
		}
	}
}
