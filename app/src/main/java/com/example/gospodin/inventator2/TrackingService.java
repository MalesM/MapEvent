package com.example.gospodin.inventator2;


import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class TrackingService extends IntentService{
    private int num = 0;
    private String radius;
    public static final String NOTIFICATION = "om.example.gospodin.inventator2";

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
        ArrayList<MarkerClass> allMarkers = MapsActivity.tinyDB.getListObject("Markers", MarkerClass.class);

        for(MarkerClass m : allMarkers){
            if(m.distance(lat, lng, m.getLat(), m.getLng()) <=  radiusInt){
                searchMarkers.add(m);
            }
        }

        num = searchMarkers.size();
        MapsActivity.tinyDB.putListObject("filteredMarkers", searchMarkers);

        sendMarkers();
    }

    public void sendMarkers(){
        Intent i = new Intent(NOTIFICATION);
        i.putExtra("HaveSome", num);
        sendBroadcast(i);
    }
}
