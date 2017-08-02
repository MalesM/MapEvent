package com.example.gospodin.inventator2;


import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class TrackingService extends IntentService{


    public TrackingService() {
        super("TrackingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        double lat = intent.getDoubleExtra("lat", 0);
        double lng = intent.getDoubleExtra("lng", 0);

        ArrayList<MarkerClass> searchMarkers = new ArrayList<>();
        ArrayList<MarkerClass> allMarkers = MapsActivity.tinyDB.getListObject("Markers", MarkerClass.class);

        for(MarkerClass m : allMarkers){
            if(m.distance(lat, lng, m.getLat(), m.getLng()) <=  200.0){
                searchMarkers.add(m);
            }
        }
    }
}
