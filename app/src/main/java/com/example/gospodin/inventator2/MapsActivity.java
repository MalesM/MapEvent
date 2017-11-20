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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import static android.widget.Toast.makeText;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        FragmentCreateUp.SendMarkerInfo, FragmentSearchUp.SendRadius,
        FragmentSettingsUp.TrackingSettings, TimePickerFragment.SendTime{

    public static final String TAG = "debuger";
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Button  confirm_button, cancel_button, cancel_button2;
    private ImageButton settingsButton, searchButton, invent_button;
    private EditText title;
    private String radius, radiusSettings = "" ;
    private boolean startService = false;
    public Location myL;
    private int firstZoom = 0, mapReady = 0, circleDraw = 0, circleFlag = 0, favoritesFlag = 0, timeShow = 0;
    private int backPress = 0;
    private int inventType;
    private Marker preparedMarker;
    private MarkerClass tempMarker;
    private Circle circle;
    public DatabaseReference flagsFB, markersFB, all, usersFB;
    public FirebaseDatabase database;
    public String key = "";
    public int timeH, timeM, currentH, currentM;
    private String time;
    private String searchTypes;
    private String typeSettings = "";
    private boolean startFromNotification = false;

    private SignInButton signInButton;
    private FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 2;
    public static String userID = "";

    //broadcast for search markers
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            usersFB.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot post : dataSnapshot.child("SearchMarkers").getChildren()){
                         drawMarker(post.getValue(MarkerClass.class));
                    }

                    if(searchTypes.contains("4")) {
                        for (DataSnapshot post : dataSnapshot.child("Favorites").getChildren()) {
                            drawFavoriteMarker(post.getValue(MarkerClass.class));
                        }
                    }
                    usersFB.child(userID).child("SearchMarkers").removeValue();
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

        if(getIntent().getBooleanExtra("notification", false))startFromNotification = true;
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        all = database.getReference();
        usersFB = database.getReference("Users");
        markersFB = database.getReference("Markers");
        //flagsFB = database.getReference("Flags");

        initViews();

        //adding gMap to app
        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mMapFragment, "Map");
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
        /*FragmentSignIn fragmentSignIn= new FragmentSignIn();
        fragmentTransaction.replace(R.id.fragment_buttons, fragmentSignIn);*/

        //auth add
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


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
                public void onStatusChanged(String s, int i, Bundle bundle) {}

                @Override
                public void onProviderEnabled(String s) {}

                @Override
                public void onProviderDisabled(String s) {}
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
                public void onStatusChanged(String s, int i, Bundle bundle) {}

                @Override
                public void onProviderEnabled(String s) {}

                @Override
                public void onProviderDisabled(String s) {}
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null) {

            Log.v(TAG,""+currentUser.getUid());
            userID = currentUser.getUid();
            //currentUserFB = database.getReference(currentUser.getUid());
            FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
            fragmentTransaction.commit();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.v(TAG,"Usao u signin");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.v(TAG, "Usao u nalog");
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MapsActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v("Service", "MAP_READY ");
        mapReady = 1;
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                timeShow = 0;
            }
        });

        //custom marker info
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.invent_info_window, null);

                TextView titleInfo = (TextView) v.findViewById(R.id.titleInfo);
                TextView descriptionInfo = (TextView) v.findViewById(R.id.descriptionInfo);
                final TextView timeInfo = (TextView) v.findViewById(R.id.timeInfo);


                titleInfo.setText(marker.getTitle());
                descriptionInfo.setText(marker.getSnippet());
                timeInfo.setText(time);

                if(timeShow == 0) {
                    markersFB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot post : dataSnapshot.getChildren()) {
                                MarkerClass m = post.getValue(MarkerClass.class);
                                if (m.getLat() == marker.getPosition().latitude && m.getLng() == marker.getPosition().longitude) {
                                    time = m.getTime();
                                    Log.v(TAG, m.getTime());
                                    timeShow = 1;
                                    marker.showInfoWindow();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                return v;
            }
        });

        //if(!userID.equals("")) {

            // adding to favorites after click on info
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(final Marker marker) {
                    if(!userID.equals("")) {
                        all.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot post : dataSnapshot.child("Markers").getChildren()) {
                                    MarkerClass m = post.getValue(MarkerClass.class);
                                    if (m.getLat() == marker.getPosition().latitude && m.getLng() == marker.getPosition().longitude) {
                                        tempMarker = m;
                                        for (DataSnapshot post2 : dataSnapshot.child("Users").child(userID).child("Favorites").getChildren()) {
                                            MarkerClass m2 = post2.getValue(MarkerClass.class);
                                            if (m.getLat() == m2.getLat() && m.getLng() == m2.getLng()) {
                                                favoritesFlag = 1;
                                            }
                                        }
                                    }
                                }
                                if (favoritesFlag == 0) {
                                    usersFB.child(userID).child("Favorites").push().setValue(tempMarker);
                                    Toast toast = makeText(getApplicationContext(), "Added to favorites", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                                    toast.show();
                                    switch (tempMarker.getType()) {
                                        case 0:
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfsport));
                                            break;
                                        case 1:
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfculture));
                                            break;
                                        case 2:
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfparty));
                                            break;
                                        case 3:
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mffood));
                                            break;
                                    }
                                } else favoritesFlag = 0;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            });

            // remove from favorites after long pres on info
            mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                @Override
                public void onInfoWindowLongClick(final Marker marker) {
                    if(!userID.equals("")) {

                        all.child("Users").child(userID).child("Favorites").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot post : dataSnapshot.getChildren()) {
                                        MarkerClass m = post.getValue(MarkerClass.class);
                                        if (m.getLat() == marker.getPosition().latitude && m.getLng() == marker.getPosition().longitude) {
                                            key = post.getKey();
                                            Toast toast = makeText(getApplicationContext(), "Removed from favorites", Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                                            toast.show();
                                            switch (m.getType()) {
                                                case 0:
                                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.msport));
                                                    break;
                                                case 1:
                                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mculture));
                                                    break;
                                                case 2:
                                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mparty));
                                                    break;
                                                case 3:
                                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfood));
                                                    break;
                                            }
                                        }

                                    }
                                    if (!key.equals("")) {
                                        all.child("Users").child(userID).child("Favorites").child(key).removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            });
        //}
        // draw all markers on map
        all.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(startFromNotification){
                    for (DataSnapshot ds: dataSnapshot.child("Users").child(userID).child("NewFromService").getChildren())
                        drawTrackedMarker(ds.getValue(MarkerClass.class));
                    all.child("Users").child(userID).child("NewFromService").removeValue();
                }else {
                    for (DataSnapshot post : dataSnapshot.child("Markers").getChildren()) {
                        String k = post.getKey();
                        MarkerClass m = post.getValue(MarkerClass.class);
                        //String[] s = m.getTime().split("[ :]");
                        Log.v(TAG, "" + m.getTimeStamp());
                        if (m.getTimeStamp() < (System.currentTimeMillis() / 1000 / 60)) {
                            markersFB.child(k).removeValue();
                        } else {
                            if(!userID.equals("")) {
                                int ii = 0;
                                for (DataSnapshot post2 : dataSnapshot.child("Users").child(userID).child("Favorites").getChildren()) {
                                    MarkerClass m2 = post2.getValue(MarkerClass.class);
                                    if (m2.getLng() == m.getLng() && m2.getLat() == m.getLat())
                                        ii++;
                                }
                                if (ii == 0) drawMarker(m);
                            }else drawMarker(m);
                        }
                    }

                    if(!userID.equals("")) {
                        for (DataSnapshot post : dataSnapshot.child("Users").child(userID).child("Favorites").getChildren()) {
                            String k = post.getKey();
                            MarkerClass m = post.getValue(MarkerClass.class);
                            //String[] s = m.getTime().split("[ :]");
                            Log.v(TAG, "" + m.getTimeStamp());
                            if (m.getTimeStamp() < (System.currentTimeMillis() / 1000 / 60)) {
                                all.child("Users").child(userID).child("Favorites").child(k).removeValue();
                            } else drawFavoriteMarker(m);
                        }
                    }
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

        final Calendar c = Calendar.getInstance();
        currentH = c.get(Calendar.HOUR_OF_DAY);
        currentM = c.get(Calendar.MINUTE);

        if(!userID.equals("")) usersFB.child(userID).child("FilteredMarkers").removeValue();
        if(!startFromNotification) usersFB.child(userID).child("NewFromService").removeValue();
        cancelAlarm();
        registerReceiver(receiver, new IntentFilter(TrackingService.NOTIFICATION));

        if(!userID.equals("")) {
            usersFB.child(userID).child("Flags").child("settingsSwitch").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    startService = dataSnapshot.getValue(boolean.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            usersFB.child(userID).child("Flags").child("settingsRadius").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    radiusSettings = Integer.toString(dataSnapshot.getValue(Integer.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v("Service", "OnPause ");

        unregisterReceiver(receiver);

        if(!userID.equals("")){
            usersFB.child(userID).child("Flags").child("settingsSwitch").setValue(startService);
            if(!radiusSettings.equals("")){
                usersFB.child(userID).child("Flags").child("settingsRadius").setValue(Integer.parseInt(radiusSettings));
            }else usersFB.child(userID).child("Flags").child("settingsRadius").setValue(0);

            usersFB.child(userID).child("Flags").child("settingsType").setValue(typeSettings);
        }
        // start tracking after closing app
        if(startService){
            markersFB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot post : dataSnapshot.getChildren()){
                        MarkerClass m = post.getValue(MarkerClass.class);
                        String a = Integer.toString(m.getType());
                        if(m.distance(myL.getLatitude(), myL.getLongitude(), m.getLat(), m.getLng()) <=  Integer.parseInt(radiusSettings) && typeSettings.contains(a)){
                            usersFB.child(userID).child("FilteredMarkers").push().setValue(m);
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

    //Click event on home screen
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
        if(!preparedMarker.getTitle().trim().equals("") && inventType!=-1 && !time.equals("")) {
            String[] s = time.split("[ :]");
            Calendar calendar = Calendar.getInstance();
            long curH = calendar.get(Calendar.HOUR_OF_DAY);
            long curM = calendar.get(Calendar.MINUTE);
            long eH = Long.parseLong(s[1]);
            long eM = Long.parseLong(s[2]);
            long curMillis = System.currentTimeMillis()/1000/60;
            long markerTime;

            if(s[0].equals("today")) {
                markerTime = (eH - curH)*60 + eM - curM + curMillis;
            } else {markerTime = (eH - curH + 24)*60 + eM - curM + curMillis;}

            MarkerClass mc = new MarkerClass(preparedMarker.getPosition().latitude, preparedMarker.getPosition().longitude,
                    preparedMarker.getTitle(), preparedMarker.getSnippet(), inventType, time, 0, markerTime);

            markersFB.push().setValue(mc);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
            fragmentTransaction.commit();
            FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
            fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
        }else {
            if(preparedMarker.getTitle().trim().equals("")){
                fragmentCreateUp.error();
            }
            else if(inventType==-1){
                Toast toast = makeText(getApplicationContext(), "Must select type", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
            else if(time.equals("")){
                Toast toast = makeText(getApplicationContext(), "Must select time", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        }
    }


    //get information from create fragment
    @Override
    public void sendInfo(String title, String detail, int type, String ttime) {
        preparedMarker.setTitle(title);
        preparedMarker.setSnippet(detail);
        inventType = type;
        time = ttime;
        switch (inventType){
            case 0:
                preparedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.msport));
                break;
            case 1:
                preparedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mculture));
                break;
            case 2:
                preparedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mparty));
                break;
            case 3:
                preparedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfood));
                break;
        }
    }

    public void initViews(){

        signInButton = (SignInButton)findViewById(R.id.SgnInBtn);
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        invent_button = (ImageButton) findViewById(R.id.invent_button);

        title = (EditText) findViewById(R.id.title);

        confirm_button = (Button) findViewById(R.id.confirm_button);
        cancel_button = (Button) findViewById(R.id.cancel_button);
        cancel_button2 = (Button) findViewById(R.id.cancel_button2);

    }

    //marker properties
    public void drawMarker(MarkerClass m){
        switch (m.getType()) {
            case 0:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.msport));
                break;
            case 1:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mculture));
                break;
            case 2:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mparty));
                break;
            case 3:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfood));
                break;
        }
    }

    public void drawFavoriteMarker(MarkerClass m){
        switch (m.getType()) {
            case 0:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfsport));
                break;
            case 1:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfculture));
                break;
            case 2:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mfparty));
                break;
            case 3:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mffood));
                break;
        }
    }

    public void drawTrackedMarker(MarkerClass m){
        switch (m.getType()) {
            case 0:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mtsport));
                break;
            case 1:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mtculture));
                break;
            case 2:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mtparty));
                break;
            case 3:
                mMap.addMarker(new MarkerOptions().position(new LatLng(m.getLat(), m.getLng())).title(m.getTitle()).snippet(m.getDescription())).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mtfood));
                break;
        }
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

        if(!searchTypes.equals("") && myL != null) {

            usersFB.child(userID).child("SearchMarkers").removeValue();
            mMap.clear();

            Intent i = new Intent(MapsActivity.this, TrackingService.class);
            i.putExtra("lat", myL.getLatitude());
            i.putExtra("lng", myL.getLongitude());
            i.putExtra("radius", radius);
            i.putExtra("user", userID);
            i.putExtra("type", searchTypes);
            startService(i);

            Toast toast = makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
            fragmentTransaction.commit();
            FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
            fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
            Log.v(TAG, searchTypes);
        }else if(searchTypes.equals("")){
            Toast toast = makeText(getApplicationContext(), "Must select type", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }else {
            Toast toast = makeText(getApplicationContext(), "Location problem", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
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

        Log.v(TAG, typeSettings);

        if(!typeSettings.equals("")) {
            usersFB.child(userID).child("Flags").child("settingsSwitch").setValue(startService);
            usersFB.child(userID).child("Flags").child("settingsRadius").setValue(Integer.parseInt(radiusSettings));
            usersFB.child(userID).child("Flags").child("settingsType").setValue(typeSettings);

            //flagsFB.child("settingsSwitch").setValue(startService);
            //flagsFB.child("settingsRadius").setValue(Integer.parseInt(radiusSettings));

            if (startService) {
                Toast toast = Toast.makeText(getApplicationContext(), "Tracking started", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

                Log.i(TAG, " " + circleDraw);

                if (circleDraw == 0) {
                    circleDraw = 1;
                    if (circleFlag == 1) {
                        circle.remove();
                    }
                    circle = mMap.addCircle(new CircleOptions()
                            .center(new LatLng(myL.getLatitude(), myL.getLongitude()))
                            .radius(Double.parseDouble(radiusSettings))
                            .strokeWidth(2)
                            .strokeColor(Color.RED)
                            .fillColor(Color.parseColor("#500084d3")));
                } else {
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
            } else {
                if (circleFlag == 1) circle.remove();
            }

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, getSupportFragmentManager().findFragmentByTag("Map"));
            fragmentTransaction.commit();
            FragmentButtonsHome fragmentButtonsHome = new FragmentButtonsHome();
            fragmentTransaction.replace(R.id.fragment_buttons, fragmentButtonsHome);
        }else{
            Toast toast = makeText(getApplicationContext(), "Must select type", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    //get radius from search Fragment
    @Override
    public void getRadius(String r, String t) {
        radius = r;
        searchTypes = t;
    }

    //get information from settings Fragment
    @Override
    public void setTracking(String radius, boolean track, String type) {
        radiusSettings = radius;
        typeSettings = type;
        startService = track;
    }

    //service check for new Invents after some time
    public void scheduleAlarrm(){
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        intent.putExtra("UserID", userID);
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

    //create time picker
    public void setTime(View view){
        TimePickerFragment timePickerFragment = TimePickerFragment.newInstance();
        timePickerFragment.show(getSupportFragmentManager(),"timePicker");
    }

    //get time from picker
    @Override
    public void inventTime(int a, int b, String d) {
        timeH = a;
        timeM = b;
        FragmentCreateUp fragmentCreateUp = (FragmentCreateUp) getSupportFragmentManager().findFragmentByTag("Details");
        fragmentCreateUp.getTime(timeH, timeM, d);
    }
}