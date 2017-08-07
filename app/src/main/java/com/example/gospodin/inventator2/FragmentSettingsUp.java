package com.example.gospodin.inventator2;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class FragmentSettingsUp extends Fragment{

    TextView distanceTextSettings;
    SeekBar seekBar2;
    Switch switch1;
    TrackingSettings trackingSettings;

    public interface TrackingSettings{
        void setTracking(String radius, boolean track);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings_up_fragment, container, false);

        distanceTextSettings = (TextView) v.findViewById(R.id.distanceTextSettings);
        switch1 = (Switch) v.findViewById(R.id.switch1);
        seekBar2 = (SeekBar) v.findViewById(R.id.seekBar2);
        seekBar2.setMax(1000);

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
        trackingSettings.setTracking(distanceTextSettings.getText().toString(), switch1.isChecked());
    }
}
