package com.example.gospodin.inventator2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyAlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.example.gospodin.inventator2";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, TrackInvents.class);
        i.putExtra("userID", intent.getStringExtra("UserID"));
        context.startService(i);
    }
}
