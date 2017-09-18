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

import com.google.android.gms.maps.model.Marker;

public class FragmentCreateUp extends Fragment {

    GetData getData;
    SendMarkerInfo markerInfo;
    private TextView duzina, sirina;
    private EditText title, detail;
    private RadioGroup radioGroup;

    public interface GetData{
        Marker getCoord();
    }

    public interface SendMarkerInfo{

        void sendInfo(String title, String detail, int type);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.invent_detail_up_fragment, container, false);
        duzina = (TextView) v.findViewById(R.id.duzina);
        sirina = (TextView) v.findViewById(R.id.sirina);

        title = (EditText) v.findViewById(R.id.title);
        detail = (EditText) v.findViewById(R.id.detail);

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

        //duzina.setText(""+getData.getCoord().getPosition().longitude);
        //sirina.setText(""+getData.getCoord().getPosition().latitude);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            getData = (GetData) context;
            markerInfo = (SendMarkerInfo) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");

        }
    }

    public void sendMarkerInfo(){
        markerInfo.sendInfo(title.getText().toString(), detail.getText().toString(), radioGroup.getCheckedRadioButtonId());
    }

    public void error(){
        title.setError("Must contain title");
        title.setBackgroundColor(Color.parseColor("#FFEBEE"));
    }
}
