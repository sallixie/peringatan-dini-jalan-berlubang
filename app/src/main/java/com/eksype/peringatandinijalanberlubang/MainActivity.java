package com.eksype.peringatandinijalanberlubang;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isServiceOk()) {
            init();
        }
    }

    private void init() {
        Toast.makeText(this, "Berhasil konek ke maps", Toast.LENGTH_SHORT).show();
    }

    public boolean isServiceOk() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Service ok: Google maps working");
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "Error but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "We can't make the request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}