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
import android.widget.TextView;

public class FragmentSearchUp extends Fragment {

    SendRadius sendRadius;
    TextView distance;
    SeekBar seekBar;
    CheckBox c1, c2, c3, c4, c5;

    public interface SendRadius{
        void getRadius(String r, String searchType);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_up_fragment, container, false);
        distance = (TextView) v.findViewById(R.id.distanceText);
        seekBar = (SeekBar) v.findViewById(R.id.seekBar);
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distance.setText(""+ i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        c1 = (CheckBox) v.findViewById(R.id.cbS);
        c2 = (CheckBox) v.findViewById(R.id.cbC);
        c3 = (CheckBox) v.findViewById(R.id.cbP);
        c4 = (CheckBox) v.findViewById(R.id.cbF);
        c5 = (CheckBox) v.findViewById(R.id.cbFav);


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            sendRadius = (SendRadius) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
    public void sendRadiusToA(){
        String s = "";
        if(c1.isChecked()){s += "0";}
        if(c2.isChecked()){s += "1";}
        if(c3.isChecked()){s += "2";}
        if(c4.isChecked()){s += "3";}
        if(c5.isChecked()){s += "4";}
        sendRadius.getRadius(distance.getText().toString(), s);
    }
}
