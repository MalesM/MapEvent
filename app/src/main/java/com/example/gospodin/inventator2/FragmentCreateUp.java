package com.example.gospodin.inventator2;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class FragmentCreateUp extends Fragment /*implements TimePickerFragment.SendTime*/ {

    SendMarkerInfo markerInfo;
    private EditText title, detail;
    private RadioGroup radioGroup;
    private TextView timeText;

    /*@Override
    public void inventTime(int a, int b) {
        if(b < 10) {
            timeText.setText("" + a + ":0" + b);
        }else{
            timeText.setText("" + a + ":" + b);
        }
    }*/


    public interface SendMarkerInfo{

        void sendInfo(String title, String detail, int type, String time);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.invent_detail_up_fragment, container, false);


        title = (EditText) v.findViewById(R.id.title);
        detail = (EditText) v.findViewById(R.id.detail);
        timeText = (TextView) v.findViewById(R.id.timeText);

        radioGroup = (RadioGroup) v.findViewById(R.id.radioGroup);

        title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    title.setBackgroundColor(Color.WHITE);
                    title.setError(null);
                }
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            markerInfo = (SendMarkerInfo) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");

        }
    }

    public void sendMarkerInfo(){
        markerInfo.sendInfo(title.getText().toString(), detail.getText().toString(),
                radioGroup.indexOfChild(getActivity().findViewById(radioGroup.getCheckedRadioButtonId())),
                timeText.getText().toString());
    }

    public void error(){
        title.setError("Must contain title");
        title.setBackgroundColor(Color.parseColor("#FFEBEE"));

    }

    public void getTime(int a, int b, String day){
        if(b < 10) {
            timeText.setText(day + " " + a + ":0" + b);
        }else{
            timeText.setText(day + " " + a + ":" + b);
        }
    }
}
