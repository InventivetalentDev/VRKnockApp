package org.inventivetalent.vrknock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

	private static final String TAG = "VRKnockScanner";
	static final int CAM_PERM_REQUEST =1897;

	private ZXingScannerView mScannerView;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAM_PERM_REQUEST);
		}else{
			startScanner();
		}
	}

	void startScanner() {
		mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
		setContentView(mScannerView);                // Set the scanner view as the content view
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == CAM_PERM_REQUEST) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startScanner();
			}else{
				Toast.makeText(this, "QR Code Scanner needs camera permissions", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if(mScannerView!=null) {
			mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
			mScannerView.startCamera();          // Start camera on resume
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(mScannerView!=null) {
			mScannerView.stopCamera();           // Stop camera on pause
		}
	}

	@Override
	public void handleResult(Result rawResult) {
		// Do something with the result here
		Log.v(TAG, rawResult.getText()); // Prints scan results
		Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

		// If you would like to resume scanning, call this method below:
//		mScannerView.resumeCameraPreview(this);

		Intent data = new Intent();
		data.putExtra("qrContent", rawResult.getText());
		data.putExtra("codeFormat", rawResult.getBarcodeFormat());
		setResult(RESULT_OK, data);
		finish();
	}
}
