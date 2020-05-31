package com.spitmca.helpinghands;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class RequestingHelp extends AppCompatActivity{

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private ProgressBar progressBar;

    ImageView profileButton;
    String stationName;


    String AIRPORT_ROAD="Airport Road";
    String ANDHERI="Andheri";
    String ASALPHA="Asalpha";
    String AZAD_NAGAR="Azad Nagar";
    String CHAKALA="Chakala";
    String D_N_NAGAR="D N Nagar";
    String JAGRUTI_NAGAR="Jagruti Nagar";
    String MAROL_NAKA="Marol Naka";
    String SAKINAKA="Sakinaka";
    String WEH="WEH";
    String VERSOVA="Versova";
    String GHATKOPAR="Ghatkopar";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requesting_help);

        profileButton=(ImageView)findViewById(R.id.requestinghelp_profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(RequestingHelp.this,ProfileActivity.class);
                startActivity(i);
            }
        });

        progressBar = findViewById(R.id.progressBar);

        Spinner spinner =findViewById(R.id.atStation);
        ArrayList<String> stationList = new ArrayList<>();
        stationList.add(VERSOVA);
        stationList.add(D_N_NAGAR);
        stationList.add(AZAD_NAGAR);
        stationList.add(ANDHERI);
        stationList.add("WESTERN EXPRESS HIGHWAY");
        stationList.add("Chakala(J.B.Nagar)");
        stationList.add(AIRPORT_ROAD);
        stationList.add(MAROL_NAKA);
        stationList.add(SAKINAKA);
        stationList.add(ASALPHA);
        stationList.add(JAGRUTI_NAGAR);
        stationList.add(GHATKOPAR);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, stationList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stationName = parent.getItemAtPosition(position).toString();
                if(stationName.equals("Western Express Highway"))
                {
                    stationName=WEH;
                }
                else if(stationName.equals("Chakala(J.B.Nagar)"))
                {
                    stationName=CHAKALA;
                }
//                Toast.makeText(parent.getContext(), "From: " + stationName, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });
        findViewById(R.id.requestinghelp_requestAssistanceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            RequestingHelp.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                }else{
                    getCurrentLocation();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        progressBar.setVisibility(View.VISIBLE);
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(RequestingHelp.this)
                .requestLocationUpdates(locationRequest, new LocationCallback(){

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices .getFusedLocationProviderClient(RequestingHelp.this)
                                .removeLocationUpdates(this);
                        if(locationResult != null && locationResult.getLocations().size()>0){
                            int latestLocationIndex = locationResult.getLocations().size() -1;
                            double latitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLongitude();

                            Intent requestedHelpLaunched =new Intent(RequestingHelp.this,HelpRequested.class);
                            requestedHelpLaunched.putExtra("latitude",latitude);
                            requestedHelpLaunched.putExtra("longitude",longitude);
                            requestedHelpLaunched.putExtra("stationName",stationName);
                            startActivity(requestedHelpLaunched);
                        }else{
                            Toast.makeText(RequestingHelp.this, "Couldn't identify your location. Please try after few minutes.", Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);

                    }
                }, Looper.getMainLooper());
    }
}
