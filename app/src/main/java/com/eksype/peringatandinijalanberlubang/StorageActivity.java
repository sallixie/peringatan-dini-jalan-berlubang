package com.eksype.peringatandinijalanberlubang;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class StorageActivity extends AppCompatActivity {

    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        tvResult = findViewById(R.id.tvResult);
        Log.i("STORAGE_PATH", Environment.getExternalStorageDirectory().getPath());

        readFile();
    }

    public void readFile() {
        if(isExternalStorageReadable()) {
            StringBuilder sb = new StringBuilder();
            try {
                File textFile = new File(Environment.getExternalStorageDirectory(), "Tes.txt");
                FileInputStream fis = new FileInputStream(textFile);

                if(fis != null) {
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader buff = new BufferedReader(isr);

                    String line = null;
                    while((line = buff.readLine()) != null) {
                        sb.append(line);
                    }
                    fis.close();
                }
                tvResult.setText(sb);
            } catch (IOException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Failed to read external storage", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isExternalStorageReadable() {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
            Log.i("State", "Yes it readable");
            return true;
        } else {
            return false;
        }
    }
}