package com.eksype.peringatandinijalanberlubang;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    TextView tvDistance, tvDistanceWarning, tvLubangDilewati;
    Switch showRiwayat;

    ArrayList<LatLng> holeLocationList;
    Polyline polyline1;
    Circle circle;
    FirebaseFirestore db;

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
        tvLubangDilewati = (TextView) findViewById(R.id.tvLubangDilewati);
        showRiwayat = (Switch) findViewById(R.id.showRiwayat);

        showRiwayat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(showRiwayat.isChecked()){
                    getLubangDilewati();
                } else {
                    hideRiwayat();
                }
            }
        });

        holeLocationList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();


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

//                    Map<String, Object> data = new HashMap<>();
//                    data.put("latitude", latitude);
//                    data.put("longitude", longitude);
//                    data.put("created_at", new Timestamp(new Date()));
//
//                    dbSaveLocation(data);
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

                Map<String, Object> data = new HashMap<>();
                data.put("latitude", latitude);
                data.put("longitude", longitude);
                data.put("created_at", new Timestamp(new Date()));

                dbSaveLubangDilewati(data);
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

//    private void dbSaveLocation(Map<String, Object> data) {
//        db.collection("perjalanan")
//                .add(data)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MapsActivity.this, "Gagal menyimpan riwayat perjalanan", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private void dbSaveLubangDilewati(Map<String, Object> data) {
        db.collection("lubang_dilewati")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity.this, "Gagal menyimpan riwayat lubang dilewati", Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    private void getData() {
//        List<LatLng> latLngs = new ArrayList<>();
//        db.collection("perjalanan")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                latLngs.add(new LatLng(document.getDouble("latitude"), document.getDouble("longitude")));
//                                Log.d("DB_DATA", document.getId() + " => " + document.getData());
//                            }
//                            for(int i=0; i<latLngs.size(); i++) {
//                                 circle = mMap.addCircle(new CircleOptions()
//                                        .center(latLngs.get(i))
//                                        .radius(10)
//                                        .fillColor(Color.rgb(0, 196, 255))
//                                        .strokeColor(Color.rgb(0, 196, 255))); // In meters
//                                circle.setTag("A");
//
////                                polyline1 = mMap.addPolyline(new PolylineOptions()
////                                        .clickable(true)
////                                        .add(latLngs.get(i)));
////                                polyline1.setTag("A");
////                                stylePolyline(polyline1);
//                            }
//                        } else {
//                            Log.w("DB_DATA", "Error getting documents.", task.getException());
//                        }
//                    }
//                });
//    }

    private void getLubangDilewati() {
        int lubangDilewati = 0;
        Query query = db.collection("lubang_dilewati");
        AggregateQuery lubangDilewatiQuery = query.count();

        lubangDilewatiQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot snapshot = task.getResult();
                    tvLubangDilewati.setText("Lubang Dilewati : " +String.valueOf(snapshot.getCount()) + " lubang");
                } else {
                    Log.d("LUBANG_DILEWATI", "Error getting documents: ", task.getException());
                }
            }
        });
    }

//    private void stylePolyline(Polyline polyline) {
//        String type = "";
//        // Get the data object stored with the polyline.
//        if (polyline.getTag() != null) {
//            type = polyline.getTag().toString();
//        }
//
//        switch (type) {
//            case "A":
//                // Use a custom bitmap as the cap at the start of the line.
//                polyline.setStartCap(new RoundCap());
//                break;
//            case "B":
//                // Use a round cap at the start of the line.
//                polyline.setStartCap(new RoundCap());
//                break;
//        }
//
//        polyline.setEndCap(new RoundCap());
//        polyline.setWidth(16);
//        polyline.setColor(Color.rgb(0, 196, 255));
//        polyline.setJointType(JointType.ROUND);
//    }

    private void hideRiwayat() {
        tvLubangDilewati.setText("Lubang Dilewati : 0 lubang");
    }

}