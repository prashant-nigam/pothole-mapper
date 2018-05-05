package project.saurabh.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;

import project.saurabh.entity.Potholes;

public class AddPotholeActivity extends Activity {

    private Button btntakephoto, btnsubmit;
    private EditText latitude,longitude;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    LocationManager service;
    File imageFile;
    static final int MY_PERMISSIONS_ACCESS_GPS = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int MY_PERMISSIONS_ACCESS_CAMERA = 3;
    Context cntx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pothole);

        latitude  = (EditText) findViewById(R.id.textLatitude);
        longitude = (EditText) findViewById(R.id.textLongitude);

        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
           // enable gps
        }//else{
            if (ContextCompat.checkSelfPermission(AddPotholeActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddPotholeActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_ACCESS_GPS);
            }else{
                try {
                   Location loc = service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(loc == null){
                        loc = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if(loc != null) {
                        latitude.setText(loc.getLatitude() + "");
                        longitude.setText(loc.getLongitude() + "");
                    }else{
                        Toast.makeText(cntx, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                }catch (SecurityException e){
                    e.printStackTrace();
                }
            }

        //}

        radioGroup=(RadioGroup)findViewById(R.id.radioGrpSeverity);

        btnsubmit=(Button)findViewById(R.id.submitpothole);

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId=radioGroup.getCheckedRadioButtonId();
                radioButton=(RadioButton)findViewById(selectedId);
                Toast.makeText(AddPotholeActivity.this,radioButton.getText(),Toast.LENGTH_SHORT).show();

                Potholes pothole = new Potholes();
                pothole.setSeverity(radioButton.getText().toString());
                if(imageFile!=null)
                pothole.setPicLocation(imageFile.getAbsolutePath());
                pothole.setLatitude(latitude.getText().toString());
                pothole.setLongitude(longitude.getText().toString());

                DBController db = new DBController(AddPotholeActivity.this);
                db.addNewPothole(pothole);
                db.close();
                finish();
            }
        });


        btntakephoto=(Button)findViewById(R.id.addPhoto);

        btntakephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    imageFile = new File(AddPotholeActivity.this.getExternalCacheDir(),
                            String.valueOf(System.currentTimeMillis()) + ".jpg");
                    Uri fileUri = Uri.fromFile(imageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } else {
                        Toast.makeText(AddPotholeActivity.this, "unable to take picture", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(cntx, "Error Taking photo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(AddPotholeActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPotholeActivity.this,
                    new String[]{Manifest.permission.CAMERA},MY_PERMISSIONS_ACCESS_CAMERA);
        }
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
                        Location loc = service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        latitude.setText(loc.getLatitude()+"");
                        longitude.setText(loc.getLongitude()+"");
                    }catch (SecurityException e){
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(AddPotholeActivity.this, "No Location permission granted ", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);*/
        }
    }
}
