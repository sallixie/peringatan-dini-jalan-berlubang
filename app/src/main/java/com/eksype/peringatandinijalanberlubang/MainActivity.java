package com.eksype.peringatandinijalanberlubang;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    Button btnMaps, btnStorage, btnSound;
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private MediaPlayer mp;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMaps = findViewById(R.id.btnMaps);
        btnStorage = findViewById(R.id.btnStorage);
        btnSound = findViewById(R.id.btnSound);

        mp = MediaPlayer.create(this, R.raw.sample100m);


        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        btnStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StorageActivity.class);
                startActivity(intent);
            }
        });

        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.start();
            }
        });

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