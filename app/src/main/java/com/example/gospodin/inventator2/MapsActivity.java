package com.example.gospodin.inventator2;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.widget.Toast.makeText;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        FragmentCreateUp.GetData, FragmentCreateUp.SendMarkerInfo, FragmentSearchUp.SendRadius,
        FragmentSettingsUp.TrackingSettings{

    public static final String TAG = "debuger";
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Button  confirm_button, cancel_button, cancel_button2;
    private ImageButton settingsButton, searchButton, invent_button;
    private EditText title;
    private String radius, radiusSettings;
    private boolean startService;
    public Location myL;
    private int firstZoom = 0, mapReady = 0, circleDraw = 0, circleFlag = 0, favoritesFlag = 0;
    private int backPress = 0;
    private int inventType;
    private Marker preparedMarker;
    private MarkerClass tempMarker;
    private Circle circle;
    public DatabaseReference flagsFB, markersFB, searchMarkers, filteredMarkers, all;
    public String key = "";


    //broadcast for search markers
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            searchMarkers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot post : dataSnapshot.getChildren()){
                         drawMarker(post.getValue(MarkerClass.class));
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        all = database.getReference();
        markersFB = database.getReference("Markers");
        flagsFB = database.getReference("Flags");
        searchMarkers = database.getReference("SearchMarkers");
        filteredMarkers = database.getReference("FilteredMarkers");


        initViews();

        //adding gMap to app
        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mMapFragment, "Map");
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.add(R.id.fragment_buttons, fragmentButtonsHome);

        //get current location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    myL = location;
                    Log.v("Location", ""+myL.getLatitude()+"  "+myL.getLongitude());
                    if(mapReady == 1 && firstZoom == 0){
                        firstZoom = 1;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myL.getLatitude(), myL.getLongitude()), 16));
                    }
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
            });
        }
        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    myL = location;
                    Log.v("Location", ""+myL.getLatitude()+"  "+myL.getLongitude());
                    if(mapReady == 1 && firstZoom == 0) {
                        firstZoom = 1;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myL.getLatitude(), myL.getLongitude()), 16));
                    }
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
            });
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v("Service", "MAP_READY ");
        mapReady = 1;
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.invent_info_window, null);

                TextView titleInfo = (TextView) v.findViewById(R.id.titleInfo);
                TextView descriptionInfo = (TextView) v.findViewById(R.id.descriptionInfo);

                titleInfo.setText(marker.getTitle());
                descriptionInfo.setText(marker.getSnippet());

                return v;
            }
        });

        // adding to favorites after click on info
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                all.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot post : dataSnapshot.child("Markers").getChildren()){
                            MarkerClass m = post.getValue(MarkerClass.class);
                            if(m.getLat() == marker.getPosition().latitude && m.getLng() == marker.getPosition().longitude){
                                tempMarker = m;
                                for (DataSnapshot post2 : dataSnapshot.child("Favorites").getChildren()) {
                                    MarkerClass m2 = post2.getValue(MarkerClass.class);
                                    if(m.getLat() == m2.getLat() && m.getLng() == m2.getLng()){favoritesFlag=1;}
                                }
                            }
                        }
                        if(favoritesFlag==0){
                            all.child("Favorites").push().setValue(tempMarker);
                            Toast toast = makeText(getApplicationContext(), "Added to favorites", Toast.LENGTH_SHORT);
                            toast.show();
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                        }else favoritesFlag = 0;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        // remove from favorites after long pres on info
        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(final Marker marker) {

                all.child("Favorites").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot post : dataSnapshot.getChildren()) {
                                MarkerClass m = post.getValue(MarkerClass.class);
                                if (m.getLat() == marker.getPosition().latitude && m.getLng() == marker.getPosition().longitude) {
                                    key = post.getKey();
                                    Toast toast = makeText(getApplicationContext(), "Removed from favorites", Toast.LENGTH_SHORT);
                                    toast.show();
                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                }

                            }
                            if(!key.equals("")){all.child("Favorites").child(key).removeValue();}
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        // draw all markers on map
        flagsFB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean haveM = dataSnapshot.child("haveMarkers").getValue(boolean.class);
                if(haveM){
                    markersFB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot post : dataSnapshot.getChildren()){
                                drawMarker(post.getValue(MarkerClass.class));
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

        filteredMarkers.removeValue();
        cancelAlarm();
        registerReceiver(receiver, new IntentFilter(TrackingService.NOTIFICATION));
        flagsFB.child("settingsSwitch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                startService = dataSnapshot.getValue(boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        flagsFB.child("settingsRadius").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                radiusSettings = Integer.toString(dataSnapshot.getValue(Integer.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v("Service", "OnPause ");
        Log.v(TAG, ""+myL.getLatitude()+" "+myL.getLongitude());
        Log.v(TAG, ""+inventType);


        unregisterReceiver(receiver);

        // start tracking after closing app
        if(startService){
            markersFB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot post : dataSnapshot.getChildren()){
                        MarkerClass m = post.getValue(MarkerClass.class);
                        if(m.distance(myL.getLatitude(), myL.getLongitude(), m.getLat(), m.getLng()) <=  Integer.parseInt(radiusSettings)){
                            filteredMarkers.push().setValue(m);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            scheduleAlarrm();
        }else{cancelAlarm();}
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
        if(!preparedMarker.getTitle().trim().equals("")) {
            MarkerClass mc = new MarkerClass(preparedMarker.getPosition().latitude, preparedMarker.getPosition().longitude,
                    preparedMarker.getTitle(), preparedMarker.getSnippet(), inventType);

            markersFB.push().setValue(mc);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
            fragmentTransaction.commit();
            FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
            fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
        }else fragmentCreateUp.error(); //last
    }

    @Override
    public Marker getCoord() {
        return preparedMarker;
    }

    //get information from create fragment
    @Override
    public void sendInfo(String title, String detail, int type) {
        preparedMarker.setTitle(title);
        preparedMarker.setSnippet(detail);
        inventType = type;
    }

    public void initViews(){
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        invent_button = (ImageButton) findViewById(R.id.invent_button);

        title = (EditText) findViewById(R.id.title);

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
        searchMarkers.removeValue();
        mMap.clear();

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
    public void settingsInvent(View view){
        backPress = 5;
        FragmentSettingsUp fragmentSettingsUp = new FragmentSettingsUp();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragmentSettingsUp, "Settings");
        fragmentTransaction.addToBackStack(null); // mozda da se izbrise!!
        fragmentTransaction.commit();

        FragmentSettingsDown fragmentSettingsDown = new FragmentSettingsDown();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentSettingsDown);
    }

    // save and track in background
    public void saveSettings(View view){
        backPress = 0;
        FragmentSettingsUp fragmentSettingsUp = (FragmentSettingsUp) getSupportFragmentManager().findFragmentByTag("Settings");
        fragmentSettingsUp.getSettings();

        flagsFB.child("settingsSwitch").setValue(startService);
        flagsFB.child("settingsRadius").setValue(Integer.parseInt(radiusSettings));

        if(startService ){
            Toast toast = Toast.makeText(getApplicationContext(), "Tracking started", Toast.LENGTH_SHORT);
            toast.show();

            Log.i(TAG, " "+circleDraw);

            if(circleDraw == 0) {
                circleDraw = 1;
                if(circleFlag == 1) {
                    circle.remove();
                }
                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(myL.getLatitude(), myL.getLongitude()))
                        .radius(Double.parseDouble(radiusSettings))
                        .strokeWidth(2)
                        .strokeColor(Color.RED)
                        .fillColor(Color.parseColor("#500084d3")));
            }else{
                circleDraw = 0;
                circleFlag = 1;
                circle.remove();
                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(myL.getLatitude(), myL.getLongitude()))
                        .radius(Double.parseDouble(radiusSettings))
                        .strokeWidth(2)
                        .strokeColor(Color.RED)
                        .fillColor(Color.parseColor("#500084d3")));

            }
        } else{if(circleFlag == 1)circle.remove();}

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
        fragmentTransaction.commit();
        FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
    }

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

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis+20*1000, 1000*20, pendingIntent);
    }

    //cancel service
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

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

    public static boolean sameMarker (MarkerClass m1, MarkerClass m2){
        return (m1.getLat() == m2.getLat() && m1.getLng() == m2.getLng());
    }


}

