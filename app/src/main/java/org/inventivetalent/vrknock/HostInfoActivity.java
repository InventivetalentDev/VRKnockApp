package org.inventivetalent.vrknock;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class HostInfoActivity extends AppCompatActivity {


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
				//TODO
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

}
