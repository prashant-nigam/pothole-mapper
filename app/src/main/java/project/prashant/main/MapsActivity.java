package project.saurabh.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import project.saurabh.entity.Potholes;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap = null;
    Button btnAddPothole, btnAutoDetect;
    TextView autoDetectTxt;
    static final int REQUEST_ADD_POTHOLE = 1;
    static final int MY_PERMISSIONS_ACCESS_GPS = 1;
    LocationManager service;
    Context cntx = this;
    private SensorManager senSensorManager = null;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1000;
    private static int TIME_THRESHOLD = 100;
    boolean autoDetect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final SensorEventListener sensorEventListener = this;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnAddPothole = (Button) findViewById(R.id.addpothole);
        btnAutoDetect = (Button) findViewById(R.id.autodetect);
        autoDetectTxt = (TextView) findViewById(R.id.autodetectTxt);

        btnAddPothole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, AddPotholeActivity.class);
                startActivityForResult(intent, REQUEST_ADD_POTHOLE);
            }
        });

        btnAutoDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!autoDetect) {
                    senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    senSensorManager.registerListener(sensorEventListener, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    autoDetectTxt.setVisibility(View.VISIBLE);
                    autoDetectTxt.setText("Auto Detect ON..!!");
                    autoDetect = true;
                }else{
                    autoDetectTxt.setVisibility(View.GONE);
                    senSensorManager.unregisterListener(sensorEventListener);
                    autoDetect = false;
                }
            }
        });

        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_ACCESS_GPS);
        }


        //senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        try{
            mMap.setMyLocationEnabled(true);
        }catch (SecurityException e){e.printStackTrace();}
        try {
            Location loc = service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc == null){
                loc = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if(loc != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 12.0f));
            }else{
                Toast.makeText(cntx, "Unable to fetch location", Toast.LENGTH_SHORT).show();
            }
        }catch (SecurityException e){e.printStackTrace();}
        showAllPotholes();

        /*mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick (Marker marker){
                marker.remove();
                return false;
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_POTHOLE) {
            showAllPotholes();
        }
    }

    private void showAllPotholes() {
        mMap.clear();
        DBController db = new DBController(MapsActivity.this);
        List<Potholes> potholesList = db.getAllPotholes();
        db.close();
        Toast.makeText(MapsActivity.this, "Potholes Size: " + potholesList.size(), Toast.LENGTH_SHORT).show();
        for (Potholes pothole : potholesList) {

            LatLng potholeLocation = new LatLng(Double.parseDouble(pothole.getLatitude()),
                    Double.parseDouble(pothole.getLongitude()));
            MarkerOptions mp;
            if (pothole.getSeverity().equalsIgnoreCase("Mild")) {
                mp = new MarkerOptions().position(potholeLocation).title("Severity" +
                        " : " + pothole.getSeverity()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            } else if (pothole.getSeverity().equalsIgnoreCase("Moderate")) {
                mp = new MarkerOptions().position(potholeLocation).title("Severity : " + pothole.getSeverity()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            } else {
                mp = new MarkerOptions().position(potholeLocation).title("Severity : " + pothole.getSeverity()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            mMap.addMarker(mp);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();
            long timeDiff = curTime - lastUpdate;
            if (timeDiff > TIME_THRESHOLD) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(z - last_z)/ diffTime * 10000;
                //float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    addPotHole(speed);
                    Toast.makeText(cntx, "Speed : "+speed+" Time:"+ timeDiff, Toast.LENGTH_SHORT).show();
                    TIME_THRESHOLD = 2000;
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }else{
                TIME_THRESHOLD = 100;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void addPotHole(float speed){
        Potholes pothole = new Potholes();
        String severity = "Mild";
        if(speed < 1700 && speed > 1400){
            severity = "Moderate";
        }else if(speed  > 1700){
            severity = "Extreme";
        }
        pothole.setSeverity(severity);
        try {
            Location loc = service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                loc = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (loc != null) {
                pothole.setLatitude(loc.getLatitude()+"");
                pothole.setLongitude(loc.getLongitude()+"");

                DBController db = new DBController(MapsActivity.this);
                db.addNewPothole(pothole);
                db.close();

                showCurrentPothole(pothole);
            }else{
                Toast.makeText(cntx, "Unable to fetch user location", Toast.LENGTH_SHORT).show();
            }
        }catch (SecurityException e){e.printStackTrace();}
    }

    public void showCurrentPothole(Potholes pothole){
        LatLng potholeLocation = new LatLng(Double.parseDouble(pothole.getLatitude()),
                Double.parseDouble(pothole.getLongitude()));
        MarkerOptions mp;
        if (pothole.getSeverity().equalsIgnoreCase("Mild")) {
            mp = new MarkerOptions().position(potholeLocation).title("Severity" +
                    " : " + pothole.getSeverity()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        } else if (pothole.getSeverity().equalsIgnoreCase("Moderate")) {
            mp = new MarkerOptions().position(potholeLocation).title("Severity : " + pothole.getSeverity()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        } else {
            mp = new MarkerOptions().position(potholeLocation).title("Severity : " + pothole.getSeverity()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        mMap.addMarker(mp);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        if(mMap != null){
                            mMap.setMyLocationEnabled(true);
                        }
                    }catch (SecurityException e){
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "No Location permission granted ", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(senSensorManager!=null)
        senSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(senSensorManager!=null)
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
