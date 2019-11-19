package org.inventivetalent.vrknock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

	public static final String TAG = "VRKnock";

	ImageButton knockButton;
	ProgressBar progressBar;
	TextView    statusTextView;

	String hostIp      = null;
	String connectCode = null;

	boolean isConnected = false;

	public static final int PORT = 16945;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		knockButton = findViewById(R.id.knockButton);
		knockButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.i(TAG, "Knock button clicked");

				sendKnock();
			}
		});

		progressBar = findViewById(R.id.progressBar);
		statusTextView = findViewById(R.id.statusTextView);

	}

	@SuppressLint("StaticFieldLeak")
	@Override
	protected void onResume() {
		super.onResume();

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		hostIp = preferences.getString("hostIp", null);
		connectCode = preferences.getString("connectCode", null);

		reconnect();
	}

	void reconnect() {
		knockButton.setEnabled(false);

		if(hostIp==null||connectCode==null)return;

		progressBar.setVisibility(View.VISIBLE);

		new ServerDiscoveryTask() {
			@Override
			protected void onPostExecute(String s) {
				if (s != null) {
					Log.i(TAG, "Found Host at " + s+":"+PORT);
					onConnectionEstablished();
				} else {
					onConnectionLost();
				}

			}
		}.execute();
	}

	void onConnectionEstablished() {
		isConnected = true;

		progressBar.setVisibility(View.INVISIBLE);
		knockButton.setEnabled(true);
		statusTextView.setText("Connected!");
	}

	void onConnectionLost() {
		isConnected = false;

		progressBar.setVisibility(View.INVISIBLE);
		knockButton.setEnabled(false);
		statusTextView.setText("Failed to connect");
	}

	void sendKnock() {
		if (!isConnected) {
			return;
		}
		//TODO: custom message
		new KnockerTask().execute(new KnockData("I am a knock!"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.reconnectItem:
				reconnect();
				return true;
			case R.id.setHostItem:
				startActivity(new Intent(this, HostInfoActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	abstract class ServerDiscoveryTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... voids) {
			if (hostIp != null) {// Try last host first
				if (testIp(hostIp)) {
					return hostIp;
				}
			}

			return null;
		}

		String host(int ip0, int ip1, int ip2, int ip3) {
			return ip0 + "." + ip1 + "." + ip2 + "." + ip3;
		}

		boolean testIp(String host) {
			Log.i("VRKnockDiscover", "Testing " + host + ":"+PORT+"...");
			try {
				URLConnection connection = new URL("http", host, PORT, "status").openConnection();
				connection.setConnectTimeout(1000);
				connection.setReadTimeout(1000);
				connection.setDoInput(true);
				InputStream in = connection.getInputStream();

				//TODO: might wanna check returned data
				return connection.getContentLength() > 4;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

		}

	}

	class KnockerTask extends AsyncTask<KnockData, Void, Boolean> {

		@Override
		protected Boolean doInBackground(KnockData... knockDatas) {
			KnockData knockData = knockDatas[0];
			Log.i("VRKnocker", "Sending Knock to " + hostIp);
			try {
				HttpURLConnection connection = (HttpURLConnection) new URL("http", hostIp, PORT, "triggerKnock").openConnection();
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");

				JSONObject jsonObject = new JSONObject();
				jsonObject.put("code", connectCode);
				jsonObject.put("message", knockData.message);

				String jsonString = jsonObject.toString();
				System.out.println(jsonString);

				OutputStream out = connection.getOutputStream();
				out.write(jsonString.getBytes("utf8"));
				out.flush();
				out.close();

				//TODO: might wanna check the returned data
				connection.getInputStream();

				return true;
			} catch (IOException | JSONException e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			super.onPostExecute(aBoolean);

			if (!aBoolean) {
				onConnectionLost();
			}
		}
	}

}
