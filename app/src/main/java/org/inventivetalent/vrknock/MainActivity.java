package org.inventivetalent.vrknock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity {

	public static final String TAG = "VRKnock";

	ImageButton knockButton;
	ProgressBar progressBar;
	TextView    statusTextView;
	TextView    activityTextView;
	EditText    messageEditText;

	String hostIp      = null;
	String connectCode = null;

	boolean isConnected = false;

	OkHttpClient client;
	WebSocket    socket;
	Callback     currentCallback = null;

	public static final int    PORT       = 16945;
	public static       String appVersion = "0.0.0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			appVersion = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		client = new OkHttpClient();

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
		activityTextView = findViewById(R.id.activityTextView);
		messageEditText = findViewById(R.id.messageEditText);

		MobileAds.initialize(this, "ca-app-pub-2604356629473365~1808998787");

		final AdView adView1 = findViewById(R.id.homeAdView);
		adView1.loadAd(new AdRequest.Builder().build());

		// ATTENTION: This was auto-generated to handle app links.
		Intent appLinkIntent = getIntent();
		String appLinkAction = appLinkIntent.getAction();
		System.out.println("appLinkAction: " + appLinkAction);
		Uri appLinkData = appLinkIntent.getData();
		System.out.println("appLinkData: " + appLinkData);
		if (appLinkData != null) {
			List<String> pathSegments = appLinkData.getPathSegments();
			System.out.println(pathSegments);
			if (pathSegments.size() > 0) {
				if (pathSegments.size() >= 2) {
					hostIp = pathSegments.get(0);
					connectCode = pathSegments.get(1);

					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString("hostIp", hostIp);
					editor.putString("connectCode", connectCode);
					editor.apply();

					Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connection_info_updated, Snackbar.LENGTH_SHORT).show();

					reconnect();
				}
			}
		}
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
		Toast.makeText(this, R.string.connecting, Toast.LENGTH_LONG).show();

		knockButton.setEnabled(false);
		statusTextView.setText(R.string.searching_host);

		if (hostIp == null || connectCode == null) {
			startHostInfoActivity();
		}

		progressBar.setVisibility(View.VISIBLE);

		new ServerDiscoveryTask().execute();
	}

	void enableButton() {
		knockButton.setImageResource(R.drawable.ic_knocking_hand_128dp);
		knockButton.setEnabled(true);
	}

	void disableButton() {
		knockButton.setEnabled(false);
		knockButton.setImageResource(R.drawable.ic_knocking_hand_grey_128dp);
	}

	void onConnectionEstablished() {
		isConnected = true;

		progressBar.setVisibility(View.INVISIBLE);
		enableButton();
		statusTextView.setText(R.string.connected);
	}

	@Deprecated
	void onConnectionLost(String reason) {
		isConnected = false;

		progressBar.setVisibility(View.INVISIBLE);
		disableButton();
		statusTextView.setText(reason);
	}

	void onConnectionLost(@StringRes int res) {
		isConnected = false;

		progressBar.setVisibility(View.INVISIBLE);
		disableButton();
		statusTextView.setText(res);
	}

	void updateActivity(String activity) {
		if (activity == null || activity.isEmpty() || "null".equals(activity)) {
			activityTextView.setText("");
		} else if ("idle".equalsIgnoreCase(activity)) {
			activityTextView.setText(R.string.currently_idle);
		} else {
			activityTextView.setText(getString(R.string.currently_playing, activity));
		}
	}

	void onConnectionLost() {
		onConnectionLost(R.string.failed_to_connect);
	}

	void sendKnock() {
		if (!isConnected) {
			return;
		}
		String message = messageEditText.getText().toString();
		if (message.length() == 0) {
			message = getString(R.string.default_notification_message);
		}
		new KnockerTask().execute(new KnockData(message));
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
				startHostInfoActivity();
				return true;
			case R.id.downloadServer:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://yeleha.co/vrknock-server-download")));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	void startHostInfoActivity() {
		startActivity(new Intent(this, HostInfoActivity.class));
	}

	static JSONObject readJson(InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		StringBuilder all = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				all.append(line);
			}
		} catch (IOException e) {
			Log.e(TAG, "Failed to read json", e);
		}

		try {
			return new JSONObject(all.toString());
		} catch (JSONException e) {
			Log.e(TAG, "Failed to parse json", e);
		}
		return null;
	}

	void sendRequest(JSONObject body, Callback callback) {
		if (MainActivity.this.socket == null) {
			callback.call(null);
			return;
		}
		if (callback != null) { currentCallback = callback; }

		MainActivity.this.socket.send(body.toString());
	}

	static JSONObject postJson(String host, int port, String path, JSONObject body) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL("http", host, port, path).openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("User-Agent", "VRKnockApp/" + appVersion);

			String jsonString = body.toString();
			System.out.println(jsonString);

			OutputStream out = connection.getOutputStream();
			out.write(jsonString.getBytes("utf8"));
			out.flush();
			out.close();

			//TODO: might wanna check the returned data
			InputStream in = connection.getInputStream();
			if (connection.getContentLength() > 2) {
				JSONObject json = readJson(in);
				if (json != null) {
					Log.i(TAG, json.toString());
					return json;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	class ServerDiscoveryTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... voids) {
			if (hostIp != null) {// Try last host first
				initSocket(hostIp);
				if (testIp(hostIp)) {
					return hostIp;
				}
			}

			return null;
		}

		void initSocket(String host) {
			if (MainActivity.this.socket != null) {
				MainActivity.this.socket.close(0,"opening new socket");
				MainActivity.this.socket=null;
			}

			Request request = new Request.Builder().url("ws://" + host + ":" + PORT).build();
			SocketListener socketListener = new SocketListener();
			MainActivity.this.socket = client.newWebSocket(request, socketListener);
			client.dispatcher().executorService().shutdown();
		}

		String host(int ip0, int ip1, int ip2, int ip3) {
			return ip0 + "." + ip1 + "." + ip2 + "." + ip3;
		}

		boolean testIp(String host) {
			Log.i("VRKnockDiscover", "Testing " + host + ":" + PORT + "...");

			try {
				JSONObject body = new JSONObject();
				body.put("action", "status");
				body.put("code", connectCode);
				body.put("version", appVersion);
				sendRequest(body, new Callback() {
					@Override
					public void call(JSONObject json) {
						if (json != null) {
							try {

								final String msg = json.getString("msg");
								final String game = json.getString("game");
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											Snackbar.make(MainActivity.this.findViewById(R.id.coordinatorLayout), msg, Snackbar.LENGTH_LONG).show();
										} catch (Exception e) {
											Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
										}
										updateActivity(game);
									}
								});

								if (json.getInt("status") != 0) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										onConnectionLost();
									}
								});
									return ;
								}

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										onConnectionEstablished();
									}
								});
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return true;
		}

	}

	class KnockerTask extends AsyncTask<KnockData, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			disableButton();
		}

		@Override
		protected Boolean doInBackground(KnockData... knockDatas) {
			KnockData knockData = knockDatas[0];
			Log.i("VRKnocker", "Sending Knock to " + hostIp);
			try {
				JSONObject body = new JSONObject();
				body.put("action", "triggerKnock");
				body.put("code", connectCode);
				body.put("version", appVersion);
				body.put("message", knockData.message);

				sendRequest(body, new Callback() {
					@Override
					public void call(JSONObject json) {
						if (json != null) {
							try {
								final String msg = json.getString("msg");
								final String game = json.getString("game");
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											Snackbar.make(MainActivity.this.findViewById(R.id.coordinatorLayout), msg, Snackbar.LENGTH_LONG).show();
										} catch (Exception e) {
											Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
										}
										updateActivity(game);
									}
								});

								if (json.getInt("status") != 0) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											onConnectionLost();
										}
									});
									return;
								}

								try {
									Thread.sleep(2000);
								} catch (InterruptedException ignored) {
								}

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										enableButton();
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					}
				});


			} catch (JSONException e) {
				e.printStackTrace();
			}
			return false;
		}

	}

	class SocketListener extends WebSocketListener {

		@Override
		public void onOpen(WebSocket webSocket, Response response) {
			super.onOpen(webSocket, response);
			System.out.println("onOpen");
		}

		@Override
		public void onMessage(WebSocket webSocket, String text) {
			super.onMessage(webSocket, text);
			System.out.println("onMessage");
			System.out.println(text);

			try {
				JSONObject parsed = new JSONObject(text);
				if (parsed.has("evt") && "status".equals(parsed.getString("evt")) && MainActivity.this.currentCallback != null) {
					MainActivity.this.currentCallback.call(parsed);
					MainActivity.this.currentCallback = null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}


		@Override
		public void onClosing(WebSocket webSocket, int code, String reason) {
			super.onClosing(webSocket, code, reason);
			System.out.println("onClosing");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onConnectionLost(R.string.server_disconnect);
				}
			});
		}

		@Override
		public void onClosed(WebSocket webSocket, int code, String reason) {
			super.onClosed(webSocket, code, reason);
			System.out.println("onClosed");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onConnectionLost(R.string.server_disconnect);
				}
			});
		}

		@Override
		public void onFailure(WebSocket webSocket, Throwable t, Response response) {
			super.onFailure(webSocket, t, response);
			System.out.println("onFailure");
			t.printStackTrace();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onConnectionLost(R.string.failed_to_connect);
				}
			});
		}
	}

	interface Callback {
		void call(JSONObject jsonObject);
	}

}
