package com.example.gospodin.inventator2;


import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackingService extends IntentService{
    private String radius;
    public static final String NOTIFICATION = "com.example.gospodin.inventator2";
    public FirebaseDatabase database = FirebaseDatabase.getInstance();
    public DatabaseReference users = database.getReference("Users");
    public DatabaseReference markersFB = database.getReference("Markers");

    public TrackingService() {
        super("TrackingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final double lat = intent.getDoubleExtra("lat", 0);
        final double lng = intent.getDoubleExtra("lng", 0);
        final String userID = intent.getStringExtra("user");
        final String types = intent.getStringExtra("type");
        radius = intent.getStringExtra("radius");
        final int radiusInt = Integer.parseInt(radius);

        markersFB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot post : dataSnapshot.getChildren()){
                    MarkerClass m = post.getValue(MarkerClass.class);
                    String a = Integer.toString(m.getType());
                    if(m.distance(lat, lng, m.getLat(), m.getLng()) <=  radiusInt && types.contains(a)){
                        users.child(userID).child("SearchMarkers").push().setValue(m);
                    }
                }
                sendMarkers();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void sendMarkers(){
        Intent i = new Intent(NOTIFICATION);
        sendBroadcast(i);
    }
}
