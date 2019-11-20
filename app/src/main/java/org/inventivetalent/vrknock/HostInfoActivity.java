package org.inventivetalent.vrknock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HostInfoActivity extends AppCompatActivity {

	static final int CODE_SCAN_REQUEST =48761;

	Button scanCodeButton;
	Button doneButton;
	EditText hostIpEditText;
	EditText codeEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_info);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HostInfoActivity.this);

		scanCodeButton = findViewById(R.id.scanCodeButton);
		scanCodeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openCodeScanner();
			}
		});
		doneButton = findViewById(R.id.doneButton);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("hostIp", hostIpEditText.getText().toString());
				editor.putString("connectCode", codeEditText.getText().toString());
				editor.apply();


				finish();
			}
		});

		hostIpEditText = findViewById(R.id.hostIpEditText);
		codeEditText = findViewById(R.id.codeEditText);

		hostIpEditText.setText(preferences.getString("hostIp", ""));
		codeEditText.setText(preferences.getString("connectCode", ""));

	}

	void openCodeScanner() {
		startActivityForResult(new Intent(this, CodeScannerActivity.class), CODE_SCAN_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (CODE_SCAN_REQUEST == requestCode) {
			if (resultCode == RESULT_OK&&data!=null) {
				String content = data.getStringExtra("qrContent");
				if (content != null&&content.startsWith("http://")) {
					/// http://1.2.3.4/code
					String[] split =content.split("/");
					if(split.length>=3) {
						String code = split[split.length - 1];
						String host = split[split.length - 2];

						hostIpEditText.setText(host);
						codeEditText.setText(code);
						Toast.makeText(this,"Info Updated!", Toast.LENGTH_SHORT).show();
					}
				}
			}else{
				Toast.makeText(this, "Failed to get code", Toast.LENGTH_LONG).show();
			}
		}
	}
}
