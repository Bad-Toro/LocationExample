package com.mac.training.locationexample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView myTxt;
    LocationManager lM;
    String provider;
    TextView myAddress;
    TextView mySpeed;
    TextView maxSpeed;
    int topSpeed = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTxt = (TextView) findViewById(R.id.txt);
        myAddress = (TextView) findViewById(R.id.myAddress);
        mySpeed = (TextView) findViewById(R.id.speed);
        maxSpeed = (TextView) findViewById(R.id.topSpeed);

        lM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria crit = new Criteria();
        provider = lM.getBestProvider(crit, false);

        checkGPSIsOn();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = lM.getLastKnownLocation(provider);

        if (location != null) {
            onLocationChanged(location);
        } else {
            myTxt.setText("Not now");
        }

    }

    void checkGPSIsOn() {
        boolean enabled = lM.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lM.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGPSIsOn();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission
                        (this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    747);
            return;
        }
        lM.requestLocationUpdates(provider, 60 * 1000, 0, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 747) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onResume();
            }
        }

    }



    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double log = location.getLongitude();
        String prov = location.getProvider();
        String addr = getAddress(lat, log);
//        String tmp = "Lat: " + lat + "\nLog: " + log + "\n" + prov
//                + "\n\n" +addr;
        String tmp = addr;

        myTxt.setText(tmp);
        int thisSpeed = (int) Math.round(location.getSpeed() * 2.23694);
        mySpeed.setText(String.valueOf(thisSpeed) + " mph");
        if (thisSpeed > topSpeed)
            topSpeed = thisSpeed;
        maxSpeed.setText("Max: " + String.valueOf(topSpeed) + " mph");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

  String getAddress(double lat, double lng) {
      Geocoder geocoder;
      List<Address> addresses;
      geocoder = new Geocoder(this, Locale.getDefault());

      try {
          addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

          if (addresses.size() > 0) {
              String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
              String city = addresses.get(0).getLocality();
              String state = addresses.get(0).getAdminArea();
              String country = addresses.get(0).getCountryName();
              String postalCode = addresses.get(0).getPostalCode();
              String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

              return address + "\n" + city + ", " + state
                      + ", " + postalCode;
          } else {
              return "NoGood";
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
   return "";
  }

    public void onNow(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Button mB = (Button) view;
        if(mB.getText().toString().equals("Go to Real Time")){
            mB.setText("Go to Save Battery");
            lM.requestLocationUpdates(provider, 100, 0, this);
        }else{
            mB.setText("Go to Real Time");
            lM.requestLocationUpdates(provider, 60 * 1000, 0, this);
        }

    }
}
