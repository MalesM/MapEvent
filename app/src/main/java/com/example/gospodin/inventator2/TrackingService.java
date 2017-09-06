package com.example.gospodin.inventator2;


import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TrackingService extends IntentService{
    private int num = 0;
    private String radius;
    public static final String NOTIFICATION = "com.example.gospodin.inventator2";
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference flagsFB = database.getReference("Flags");
    private DatabaseReference markersFB = database.getReference("Markers");
    private DatabaseReference fmarkersFB = database.getReference("SearchMarkers");
    private ArrayList<MarkerClass> allMarkers = new ArrayList<>();

    public TrackingService() {
        super("TrackingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        double lat = intent.getDoubleExtra("lat", 0);
        double lng = intent.getDoubleExtra("lng", 0);
        radius = intent.getStringExtra("radius");
        int radiusInt = Integer.parseInt(radius);

        ArrayList<MarkerClass> searchMarkers = new ArrayList<>();
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
                                allMarkers.add(mc);
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

        for(MarkerClass m : allMarkers){
            if(m.distance(lat, lng, m.getLat(), m.getLng()) <=  radiusInt){
                searchMarkers.add(m);
            }
        }

        num = searchMarkers.size();
        if(!searchMarkers.isEmpty()){
            for(MarkerClass m : searchMarkers){
                fmarkersFB.push().setValue(m);
            }
        }

        sendMarkers();
    }

    public void sendMarkers(){
        Intent i = new Intent(NOTIFICATION);
        i.putExtra("HaveSome", num);
        sendBroadcast(i);
    }
}
