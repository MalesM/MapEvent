package com.example.gospodin.inventator2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        FragmentCreateUp.GetData, FragmentCreateUp.SendMarkerInfo{

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Button invent_button, confirm_button, cancel_button, cancel_button2;
    private ImageButton settingsButton, searchButton;
    private String provider;
    public Location myL;
    private int firstZoom = 0, mapReady = 0;
    private int mapReadySaved;
    private float zoomLevel = 16;
    private ArrayList<MarkerClass> markers = new ArrayList<MarkerClass>();
    private boolean saved = true;
    private int click = 0, drag = 0;
    private double llat, llng;
    private Marker preparedMarker;
    static TinyDB tinyDB;
    public MarkerClass markerClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tinyDB = new TinyDB(getApplicationContext());

        initViews();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mMapFragment, "Map");
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.add(R.id.fragment_buttons, fragmentButtonsHome);




        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = 1;
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        if(tinyDB.getBoolean("haveMarkers")){
            for(MarkerClass m : markers){
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription()));
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                preparedMarker = marker;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        if(tinyDB.getBoolean("haveMarkers")){
            markers = tinyDB.getListObject("Markers", MarkerClass.class);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);

        tinyDB.putListObject("Markers", markers);
        tinyDB.putBoolean("haveMarkers", true);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        myL = location;
        if(firstZoom == 0 && mapReady == 1){
            firstZoom = 1;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),zoomLevel));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    //Click invent on home screen
    public void createInvent(View view){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                preparedMarker = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                FragmentConfirm fragmentConfirm = new FragmentConfirm();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_buttons, fragmentConfirm);
                fragmentTransaction.commit();
                mMap.setOnMapClickListener(null);
            }
        });

        FragmentMapClick fragmentMapClick = new FragmentMapClick();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentMapClick);
        fragmentTransaction.commit();
    }

    //Go to details after pick location
    public void inventDetails(View view){

        preparedMarker.setDraggable(false);
        FragmentCreateUp fragmentCreateUp = new FragmentCreateUp();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentCreateUp, "Details");
        fragmentTransaction.addToBackStack(null); // mozda da se izbrise!!
        fragmentTransaction.commit();

        FragmentCreateDown fragmentCreateDown = new FragmentCreateDown();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentCreateDown);


    }

    //Cancel after first press
    public void cancelCreation(View view){
        preparedMarker.remove();
        FragmentButtonsHome fragmentButtonsHome= new FragmentButtonsHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
        fragmentTransaction.commit();
    }

    //Cancel in detail invent
    public void cancelInvent(View view){

        preparedMarker.remove();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
        fragmentTransaction.commit();
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);

    }

    //fully invent create
    public void finishInvent(View view){

        FragmentCreateUp fragmentCreateUp = (FragmentCreateUp) getSupportFragmentManager().findFragmentByTag("Details");
        fragmentCreateUp.sendMarkerInfo();

        markers.add(new MarkerClass(preparedMarker.getPosition().latitude, preparedMarker.getPosition().longitude,
                preparedMarker.getTitle(), preparedMarker.getSnippet()));

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
        fragmentTransaction.commit();
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);


    }

    @Override
    public Marker getCoord() {
        return preparedMarker;
    }

    @Override
    public void sendInfo(String title, String detail) {
        preparedMarker.setTitle(title);
        preparedMarker.setSnippet(detail);
    }

    public void initViews(){
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        invent_button = (Button) findViewById(R.id.invent_button);

        confirm_button = (Button) findViewById(R.id.confirm_button);
        cancel_button = (Button) findViewById(R.id.cancel_button);
        cancel_button2 = (Button) findViewById(R.id.cancel_button2);

    }

    // filter with service
    public void searchInventa(View view){
        Intent i = new Intent(MapsActivity.this, TrackingService.class);
        i.putExtra("lat", myL.getLatitude());
        i.putExtra("lng", myL.getLongitude());
        startService(i);
    }

    //filter with thread
    public void searchInvent(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }
}

