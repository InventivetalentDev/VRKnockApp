package org.inventivetalent.vrknock;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

	private TextView versionTextView;
	private TextView versionCodeTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_about);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}


		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

			versionTextView = findViewById(R.id.versionTextView);
			versionTextView.setText(getResources().getString(R.string.about_version, pInfo.versionName));
			versionCodeTextView = findViewById(R.id.versionCodeTextView);
			versionCodeTextView.setText(String.valueOf(pInfo.versionCode) + " (" + (BuildConfig.DEBUG ? "debug" : "release") + ")");
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			Log.d("VRKnock", "PackageManager Catch : " + e.toString());
		}

	}

}
