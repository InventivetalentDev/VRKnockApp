package org.inventivetalent.vrknock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class HostInfoActivity extends AppCompatActivity {

	static final int CODE_SCAN_REQUEST = 48761;

	Button   scanCodeButton;
	Button   doneButton;
	EditText hostIpEditText;
	EditText codeEditText;

	ConnectionMethod connectionMethod = ConnectionMethod.DIRECT;

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

				String host = hostIpEditText.getText().toString();
				String code = codeEditText.getText().toString();

				if (host.length() > 0) { editor.putString("host", host); }
				if (code.length() > 0) { editor.putString("connectCode", code); }
				if (connectionMethod != null) { editor.putString("connectionMethod", connectionMethod.name()); }
				editor.apply();

				finish();
			}
		});

		hostIpEditText = findViewById(R.id.hostIpEditText);
		codeEditText = findViewById(R.id.codeEditText);

		hostIpEditText.setText(preferences.getString("host", ""));
		codeEditText.setText(preferences.getString("connectCode", ""));
		connectionMethod = ConnectionMethod.valueOf(preferences.getString("connectionMethod", "DIRECT"));

		// ATTENTION: This was auto-generated to handle app links.
		Intent appLinkIntent = getIntent();
		String appLinkAction = appLinkIntent.getAction();
		System.out.println("appLinkAction: " + appLinkAction);
		Uri appLinkData = appLinkIntent.getData();
		System.out.println("appLinkData: " + appLinkData);
	}

	void openCodeScanner() {
		startActivityForResult(new Intent(this, CodeScannerActivity.class), CODE_SCAN_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (CODE_SCAN_REQUEST == requestCode) {
			if (resultCode == RESULT_OK && data != null) {
				String content = data.getStringExtra("qrContent");
				if (content != null && (content.startsWith("http://") || content.startsWith("https://"))) {
					Uri uri = Uri.parse(content);
					KnockUrlParser.ParsedKnockInfo knockInfo = KnockUrlParser.parse(uri);
					if (knockInfo == null) {
						Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.failed_get_code, Snackbar.LENGTH_LONG).show();
						return;
					}
					System.out.println(knockInfo);

					hostIpEditText.setText(knockInfo.host);
					codeEditText.setText(knockInfo.code);
					connectionMethod = knockInfo.connectionMethod;
					Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connection_info_updated, Snackbar.LENGTH_SHORT).show();
				}
			} else {
				Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.failed_get_code, Snackbar.LENGTH_LONG).show();
			}
		}
	}
}
