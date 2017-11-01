package com.example.gospodin.inventator2;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentSettingsUp extends Fragment{

    TextView distanceTextSettings;
    SeekBar seekBar2;
    Switch switch1;
    TrackingSettings trackingSettings;
    CheckBox c1, c2, c3, c4;

    public interface TrackingSettings{
        void setTracking(String radius, boolean track, String typeS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings_up_fragment, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersFlags = database.getReference("Users").child(MapsActivity.userID).child("Flags");
        distanceTextSettings = (TextView) v.findViewById(R.id.distanceTextSettings);
        switch1 = (Switch) v.findViewById(R.id.switch1);
        seekBar2 = (SeekBar) v.findViewById(R.id.seekBar2);
        seekBar2.setMax(1000);

        usersFlags.child("settingsRadius").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = dataSnapshot.getValue(Integer.class);
                distanceTextSettings.setText(""+i);
                seekBar2.setProgress(i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersFlags.child("settingsSwitch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                switch1.setChecked(dataSnapshot.getValue(Boolean.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distanceTextSettings.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        c1 = (CheckBox) v.findViewById(R.id.cbSt);
        c2 = (CheckBox) v.findViewById(R.id.cbCt);
        c3 = (CheckBox) v.findViewById(R.id.cbPt);
        c4 = (CheckBox) v.findViewById(R.id.cbFt);


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            trackingSettings = (TrackingSettings) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public void getSettings(){
        String s = "";
        if(c1.isChecked()){s += "0";}
        if(c2.isChecked()){s += "1";}
        if(c3.isChecked()){s += "2";}
        if(c4.isChecked()){s += "3";}
        trackingSettings.setTracking(distanceTextSettings.getText().toString(), switch1.isChecked(), s);
    }
}
