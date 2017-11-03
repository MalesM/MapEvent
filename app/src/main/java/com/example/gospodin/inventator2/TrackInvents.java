package com.example.gospodin.inventator2;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
    //private String id = MapsActivity.userID;


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
    protected void onHandleIntent(@Nullable final Intent intent) {

        final String id = intent.getStringExtra("userID");
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        allFB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                distance = dataSnapshot.child("Users").child(id).child("Flags").child("settingsRadius").getValue(Integer.class);
                                long size = dataSnapshot.child("Users").child(id).child("FilteredMarkers").getChildrenCount();
                                String types = dataSnapshot.child("Users").child(id).child("Flags").child("settingsType").getValue(String.class);
                                //Log.i(TAG, ""+distance);
                                //Log.i(TAG, ""+size);
                                for(DataSnapshot markers : dataSnapshot.child("Markers").getChildren()){
                                    MarkerClass m = markers.getValue(MarkerClass.class);
                                    String a = Integer.toString(m.getType());
                                    if(m.distance(myL.getLatitude(), myL.getLongitude(), m.getLat(), m.getLng()) <= distance && types.contains(a)){
                                        for(DataSnapshot filter : dataSnapshot.child("Users").child(id).child("FilteredMarkers").getChildren()){
                                            MarkerClass mm = filter.getValue(MarkerClass.class);
                                            if (m.getLat() == mm.getLat() && m.getLng() == mm.getLng()){
                                                contains++;
                                                Log.i(TAG, " "+m.getLat()+" "+m.getLng());
                                                Log.i(TAG, " "+mm.getLat()+" "+mm.getLng());
                                            }
                                        }

                                        Log.i(TAG, ""+contains);
                                        Log.i(TAG, ""+size);
                                        if(contains == 0){
                                            allFB.child("Users").child(id).child("FilteredMarkers").push().setValue(m);
                                            allFB.child("Users").child(id).child("NewFromService").push().setValue(m);
                                            //allFB.child("NewFromService").push().setValue(m);
                                            contains = 0;
                                            size++;
                                            added++;
                                        }else contains = 0;
                                    }
                                }
                                if(added != 0){
                                    added += dataSnapshot.child("Users").child(id).child("NewFromService").getChildrenCount();
                                    Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                                    i.putExtra("notification", true);
                                    PendingIntent pendingIntent =
                                            PendingIntent.getActivity(getApplicationContext(),0, i, PendingIntent.FLAG_UPDATE_CURRENT );

                                    NotificationCompat.Builder builder =
                                            new NotificationCompat.Builder(getApplicationContext())
                                                    .setSmallIcon(R.drawable.search)
                                                    .setContentTitle("New Invent!")
                                                    .setContentText("total: "+ added)
                                                    .setAutoCancel(true)
                                                    .setContentIntent(pendingIntent);
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
