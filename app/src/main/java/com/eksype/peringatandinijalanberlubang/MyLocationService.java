package com.eksype.peringatandinijalanberlubang;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

public class MyLocationService extends BroadcastReceiver {

    public static final String ACTION_PROCESS_UPDATE="edmt.dev.googlelocationbackground.UPDATE_LOCATION";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            final String action = intent.getAction();
            if(ACTION_PROCESS_UPDATE.equals(action)) {
                LocationResult result =  LocationResult.extractResult(intent);
                if(result != null) {
                    Location location = result.getLastLocation();
                    float latitude = (float) location.getLatitude();
                    float longitude = (float) location.getLongitude();
                    String locationString = new StringBuilder(""+location.getLatitude())
                            .append("/")
                            .append(location.getLongitude())
                            .toString();
                    try {
                        Toast.makeText(context, locationString, Toast.LENGTH_SHORT).show();
                        UpdateLocationActivity.getInstance().updateTextView(locationString, latitude, longitude);
                    } catch (Exception e) {
                        Toast.makeText(context, locationString, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}