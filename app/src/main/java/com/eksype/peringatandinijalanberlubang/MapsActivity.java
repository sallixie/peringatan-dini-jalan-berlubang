package com.eksype.peringatandinijalanberlubang;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.InputStream;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback locationCallback;

    private MediaPlayer mp100, mp50, mp20;
    TextView tvDistance, tvDistanceWarning;

    ArrayList<LatLng> holeLocationList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mp100 = MediaPlayer.create(this, R.raw.sample100m);
        mp50 = MediaPlayer.create(this, R.raw.sample50m);
        mp20 = MediaPlayer.create(this, R.raw.sample20m);

        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDistanceWarning = (TextView) findViewById(R.id.tvDistanceWarning);

        holeLocationList = new ArrayList<>();


        String data = "";
        StringBuffer sbuffer = new StringBuffer();
        InputStream is = this.getResources().openRawResource(R.raw.data_koordinat);

        try {
            byte[] buffer = new byte[is.available()];
            while (is.read(buffer) != -1) {
                data = new String(buffer);
            }
            String[] rows = data.split("\n");
            for (int i = 0; i < rows.length; i++) {
                String[] columns = rows[i].split(",");
                holeLocationList.add(new LatLng(Double.parseDouble(columns[0]), Double.parseDouble(columns[1])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i(TAG, location.toString());
                }
            }
        };

        getLocationPermission();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

            for (int i = 0; i < holeLocationList.size(); i++){
                MarkerOptions markerOptions = new MarkerOptions().position(holeLocationList.get(i)).title("Hole " + (i+1));
//                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.marker);
                int height = bitmapDrawable.getBitmap().getHeight();
                int width = bitmapDrawable.getBitmap().getWidth();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width/10, height/10, false)));
                mMap.addMarker(markerOptions);
            }

            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    distanceToHole(latitude, longitude);
                }
            });
        }
    }

    private void distanceToHole(double latitude, double longitude){
        Location myLocation = new Location("My Location");
        myLocation.setLatitude(latitude);
        myLocation.setLongitude(longitude);

        float distance = 1000000000;
        for (int i = 0; i < holeLocationList.size(); i++){
            Location holeLocation = new Location("Hole Location");
            holeLocation.setLatitude(holeLocationList.get(i).latitude);
            holeLocation.setLongitude(holeLocationList.get(i).longitude);
            float distanceTemp = myLocation.distanceTo(holeLocation);
            if (distanceTemp < distance){
                distance = distanceTemp;
            }
        }

        if(distance < 300){
            tvDistance.setText((int) distance + " m");
            tvDistanceWarning.setText("Hati-hati, ada lubang di dekat anda!");

            if(distance <= 100 && distance > 50) {
                mp100.start();
            } else if (distance <= 50 && distance > 20) {
                mp50.start();
            } else if (distance <= 20 && distance > 0) {
                mp20.start();
            }

        } else {
            tvDistance.setText("> 300 m");
            tvDistanceWarning.setText("Lubang tidak terdeteksi di sekitar anda");
        }
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                        }else{
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission(){
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }
}