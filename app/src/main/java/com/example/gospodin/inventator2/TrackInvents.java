package com.example.gospodin.inventator2;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class TrackInvents extends IntentService implements LocationListener {

    private int radius;
    private LocationManager locationManager;
    private String provider;
    public Location myL;
    private static ArrayList<MarkerClass> trackNew;
    private static ArrayList<MarkerClass> all;



    public TrackInvents() {
        super("TrackInvents");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*tdb = new TinyDB(getApplicationContext());
        //preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!tdb.getBoolean("noFiltered")) {
            trackNew = tdb.getListObject("filteredMarkers", MarkerClass.class);
        }else{trackNew = new ArrayList<>();}
        all = tdb.getListObject("Markers", MarkerClass.class);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);*/
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //String radiusS = preferences.getString("radiusSettings", "");
       /* String radiusS2 = tdb.getString("radiusSettings");
        radius = Integer.parseInt(radiusS2);
        int a = 0;
        int b = 0;
        int c =0;
        //int radius2 = Integer.parseInt(radiusS2);
        Location current = myL;

        for(MarkerClass m : all){
            if(m.distance(current.getLatitude(), current.getLongitude(), m.getLat(), m.getLng()) <= radius) {
                a++;
                for (MarkerClass mm : trackNew) {
                    if (m.distance(m.getLat(),m.getLng(), mm.getLat(), mm.getLng()) == 0) {
                        b++;
                    }
                }
            }
            if(a != (b+c)){
                c++;
                trackNew.add(m);}
        }

        if(a != b){
            tdb.putListObject("filteredMarkers", trackNew);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.search)
                            .setContentTitle("New Invent!")
                            .setContentText("total: "+c);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(12345, builder.build());
        }*/

        Log.v("Service", "Radius is "+radius);
       /* Log.v("Service", "A is "+a);
        Log.v("Service", "b is "+b);
        Log.v("Service", "Running "+ myL.getLongitude()+" "+myL.getLatitude());*/

    }

    @Override
    public void onLocationChanged(Location location) {
        myL = location;
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

}
