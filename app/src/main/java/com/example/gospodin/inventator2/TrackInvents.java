package com.example.gospodin.inventator2;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackInvents extends IntentService  {

    public static final String TAG = "debuger";
    private LocationManager locationManager;
    public Location myL;
    public DatabaseReference allFB,flagsFB, markersFB, filteredMarkers;
    int distance, added = 0, contains = 0;


    public TrackInvents() {
        super("TrackInvents");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        allFB = database.getReference();
        markersFB = database.getReference("Markers");
        flagsFB = database.getReference("Flags");
        filteredMarkers = database.getReference("FilteredMarkers");


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    myL = location;
                    Log.i(TAG, "NP "+myL.getLatitude()+" "+myL.getLongitude());
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
                    Log.i(TAG, "GPS "+myL.getLatitude()+" "+myL.getLongitude());

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
    protected void onHandleIntent(@Nullable Intent intent) {

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        allFB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                distance = dataSnapshot.child("Flags").child("settingsRadius").getValue(Integer.class);
                                long size = dataSnapshot.child("FilteredMarkers").getChildrenCount();
                                //Log.i(TAG, ""+distance);
                                //Log.i(TAG, ""+size);
                                for(DataSnapshot markers : dataSnapshot.child("Markers").getChildren()){
                                    MarkerClass m = markers.getValue(MarkerClass.class);
                                    if(m.distance(myL.getLatitude(), myL.getLongitude(), m.getLat(), m.getLng()) <= distance){
                                        for(DataSnapshot filter : dataSnapshot.child("FilteredMarkers").getChildren()){
                                            MarkerClass mm = filter.getValue(MarkerClass.class);
                                            if (m.distance(m.getLat(),m.getLng(), mm.getLat(), mm.getLng()) == 0){
                                                contains++;
                                                Log.i(TAG, " "+m.getLat()+" "+m.getLng());
                                                Log.i(TAG, " "+mm.getLat()+" "+mm.getLng());
                                            }
                                        }

                                        Log.i(TAG, ""+contains);
                                        Log.i(TAG, ""+size);
                                        if(contains == 0){
                                            filteredMarkers.push().setValue(m);
                                            contains = 0;
                                            size++;
                                            added++;
                                        }else contains = 0;
                                    }
                                }
                                if(added != 0){
                                    NotificationCompat.Builder builder =
                                            new NotificationCompat.Builder(getApplicationContext())
                                                    .setSmallIcon(R.drawable.search)
                                                    .setContentTitle("New Invent!")
                                                    .setContentText("total: "+added);
                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    // notificationID allows you to update the notification later on.
                                    mNotificationManager.notify(12345, builder.build());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                },
                5000
        );



    }

}
