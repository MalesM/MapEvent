package com.example.gospodin.inventator2;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.widget.Toast.makeText;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        FragmentCreateUp.GetData, FragmentCreateUp.SendMarkerInfo, FragmentSearchUp.SendRadius,
        FragmentSettingsUp.TrackingSettings{

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Button invent_button, confirm_button, cancel_button, cancel_button2;
    private ImageButton settingsButton, searchButton;
    private String provider;
    private String radius, radiusSettings;
    private boolean startService;
    public Location myL;
    private int firstZoom = 0, mapReady = 0, circleDraw = 0;
    private int backPress = 0;
    private float zoomLevel = 16;
    private ArrayList<MarkerClass> markers = new ArrayList<MarkerClass>();
    private ArrayList<MarkerClass> filteredMarkers = new ArrayList<MarkerClass>();
    private Marker preparedMarker;
    private Circle circle;
    public DatabaseReference flagsFB, markersFB, searchMarkers;


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int num = intent.getIntExtra("HaveSome", 0);
            if (num != 0){
                searchMarkers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot post : dataSnapshot.getChildren()){
                            MarkerClass mc = post.getValue(MarkerClass.class);
                            filteredMarkers.add(mc);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mMap.clear();
                for(MarkerClass m : filteredMarkers){
                    drawMarker(m);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        markersFB = database.getReference("Markers");
        flagsFB = database.getReference("Flags");
        searchMarkers = database.getReference("SearchMarkers");

        flagsFB.child("haveMarkers").setValue(true);

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

        if(!markers.isEmpty()){
            for(MarkerClass m : markers)
                drawMarker(m);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                preparedMarker = marker;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("Service", "OnResume ");

        flagsFB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean haveM = dataSnapshot.child("haveMarkers").getValue(boolean.class);
                if(haveM){
                    markersFB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot post : dataSnapshot.getChildren()){
                                MarkerClass mc = post.getValue(MarkerClass.class);
                                markers.add(mc);
                                drawMarker(mc);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        cancelAlarm();
        registerReceiver(receiver, new IntentFilter(TrackingService.NOTIFICATION));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v("Service", "OnPause ");
        locationManager.removeUpdates(this);

        unregisterReceiver(receiver);

        /*tinyDB.putListObject("Markers", markers);
        tinyDB.putBoolean("haveMarkers", true);

        if(tinyDB.getBoolean("Switch")){
            filterMarkers(markers);
            scheduleAlarrm();
        }else{cancelAlarm();}*/
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
        makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }


    //Click invent on home screen
    public void createInvent(View view){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                backPress = 2;
                preparedMarker = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                FragmentConfirm fragmentConfirm = new FragmentConfirm();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_buttons, fragmentConfirm);
                //fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mMap.setOnMapClickListener(null);
            }
        });

        backPress = 1;
        FragmentMapClick fragmentMapClick = new FragmentMapClick();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentMapClick);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    //Go to details after pick location
    public void inventDetails(View view){

        backPress = 3;
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
        backPress = 0;
        preparedMarker.remove();
        FragmentButtonsHome fragmentButtonsHome= new FragmentButtonsHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
        fragmentTransaction.commit();
    }

    //Cancel in detail invent
    public void cancelInvent(View view){
        backPress = 0;
        preparedMarker.remove();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
        fragmentTransaction.commit();
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);

    }

    //Cancel in search window
    public void cancelfInvent(View view){
        backPress = 0;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
        fragmentTransaction.commit();
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
    }

    //Cancel in settings window
    public void cancelSInvent(View view){
        backPress = 0;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
        fragmentTransaction.commit();
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
    }

    //fully invent create
    public void finishInvent(View view){

        backPress = 0;
        FragmentCreateUp fragmentCreateUp = (FragmentCreateUp) getSupportFragmentManager().findFragmentByTag("Details");
        fragmentCreateUp.sendMarkerInfo();

        MarkerClass mc = new MarkerClass(preparedMarker.getPosition().latitude, preparedMarker.getPosition().longitude,
                preparedMarker.getTitle(), preparedMarker.getSnippet());
        markers.add(mc);
        markersFB.push().setValue(mc);

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

    //get information from create fragment
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

    public void drawMarker(MarkerClass m){
        mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription()));
    }

    // go to search fragment
    public void searchInvent(View view){
        backPress = 4;
        FragmentSearchUp fragmentSearchUp = new FragmentSearchUp();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentSearchUp, "Search");
        fragmentTransaction.addToBackStack(null); // mozda da se izbrise!!
        fragmentTransaction.commit();

        FragmentSearchDown fragmentSearchDown = new FragmentSearchDown();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentSearchDown);
    }

    // apply radius
    public void searchfInvent(View view){
        backPress = 0;
        FragmentSearchUp fragmentSearchUp = (FragmentSearchUp) getSupportFragmentManager().findFragmentByTag("Search");
        fragmentSearchUp.sendRadiusToA();

        Intent i = new Intent(MapsActivity.this, TrackingService.class);
        i.putExtra("lat", myL.getLatitude());
        i.putExtra("lng", myL.getLongitude());
        i.putExtra("radius", radius);
        startService(i);

        Toast toast = makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT);
        toast.show();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
        fragmentTransaction.commit();
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
    }

    //go to settings fragment
    /*public void settingsInvent(View view){
        backPress = 5;
        FragmentSettingsUp fragmentSettingsUp = new FragmentSettingsUp();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentSettingsUp, "Settings");
        fragmentTransaction.addToBackStack(null); // mozda da se izbrise!!
        fragmentTransaction.commit();

        FragmentSettingsDown fragmentSettingsDown = new FragmentSettingsDown();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentSettingsDown);
    }*/

    // save and track in background
   /* public void saveSettings(View view){
        backPress = 0;
        FragmentSettingsUp fragmentSettingsUp = (FragmentSettingsUp) getSupportFragmentManager().findFragmentByTag("Settings");
        fragmentSettingsUp.getSettings();

        tinyDB.putBoolean("Switch", startService);
        tinyDB.putString("radiusSettings", radiusSettings);



        if(startService ){
            Toast toast = Toast.makeText(getApplicationContext(), "Tracking started", Toast.LENGTH_SHORT);
            toast.show();

            if(circleDraw == 0) {
                circleDraw = 1;
                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(myL.getLatitude(), myL.getLongitude()))
                        .radius(Double.parseDouble(radiusSettings))
                        .strokeColor(Color.RED)
                        .fillColor(Color.BLUE));
            }else{
                circleDraw = 0;
                circle.remove();
                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(myL.getLatitude(), myL.getLongitude()))
                        .radius(Double.parseDouble(radiusSettings))
                        .strokeColor(Color.RED)
                        .fillColor(Color.BLUE));

            }
        } else{if(circleDraw == 1)circle.remove();}

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
        fragmentTransaction.commit();
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
    }*/

    //get radius from search Fragment
    @Override
    public void getRadius(String r) {
        radius = r;
    }

    //get information from settings Fragment
    @Override
    public void setTracking(String radius, boolean track) {
        radiusSettings = radius;
        startService = track;
    }

    //service check for new Invents after some time
    public void scheduleAlarrm(){
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 1000*60*2, pendingIntent);
    }

    //cancel service
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    //filter Invents in search radius
    /*public void filterMarkers(ArrayList<MarkerClass> markersAll){
        int i = 0;
        filteredMarkers.clear();
        for(MarkerClass m : markersAll){
            if(m.distance(myL.getLatitude(), myL.getLongitude(), m.getLat(), m.getLng()) <=  Integer.parseInt(radiusSettings)){
                filteredMarkers.add(m);
                i++;
            }
        }
        if(i != 0) {
            tinyDB.putListObject("filteredMarkers", filteredMarkers);
        }else{tinyDB.putBoolean("noFiltered", true);}
    }*/

    @Override
    public void onBackPressed() {
        switch (backPress){
            case 0: finish();
                    break;
            case 1:
                backPress = 0;
                FragmentButtonsHome fragmentButtonsHome= new FragmentButtonsHome();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
                fragmentTransaction.commit();
                break;
            case 2:
                backPress = 0;
                preparedMarker.remove();
                fragmentButtonsHome = new FragmentButtonsHome();
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
                fragmentTransaction.commit();
                break;
            case 3:
                backPress = 0;
                preparedMarker.remove();
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
                fragmentTransaction.commit();
                fragmentButtonsHome = new FragmentButtonsHome();
                fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
                break;
            case 4:
                backPress = 0;
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
                fragmentTransaction.commit();
                fragmentButtonsHome = new FragmentButtonsHome();
                fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
                break;
            case 5:
                backPress = 0;
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
                fragmentTransaction.commit();
                fragmentButtonsHome = new FragmentButtonsHome();
                fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
                break;
        }
    }
}

