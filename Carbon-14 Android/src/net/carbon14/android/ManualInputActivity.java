
package net.carbon14.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public final class ManualInputActivity extends Activity {
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.input_dialog);

		Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(mDoneListener);
	}

	private final Button.OnClickListener mDoneListener = new Button.OnClickListener() {
		public void onClick(View view) {
			EditText editor = (EditText) findViewById(R.id.barcodeInputEditText);
			
			Intent result = new Intent(getIntent().getAction());
			String barcode = editor.getText().toString();
			result.putExtra("BARCODE", barcode);
		    setResult(RESULT_OK, result);

			finish();
		}
	};
}
