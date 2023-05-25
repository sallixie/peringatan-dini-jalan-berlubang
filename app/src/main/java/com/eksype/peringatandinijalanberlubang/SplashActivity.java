package com.eksype.peringatandinijalanberlubang;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(
            new Runnable() {
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
                    startActivity(intent);
                }
            },
        3000);
    }
}