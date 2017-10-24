package com.example.gospodin.inventator2;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;


public class TimePickerFragment extends DialogFragment /*implements TimePickerDialog.OnTimeSetListener*/ {

    SendTime sendTime;
    TimePicker tp;
    Button tod, tom;

    public interface SendTime{
        void inventTime(int a, int b, String day);
    }

    public TimePickerFragment(){}

    public static TimePickerFragment newInstance() {

        Bundle args = new Bundle();

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_time_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tp = (TimePicker) view.findViewById(R.id.timePicker);
        tod = (Button) view.findViewById(R.id.todBtn);
        tom = (Button) view.findViewById(R.id.tomBtn);

        tod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTime.inventTime(tp.getCurrentHour(), tp.getCurrentMinute(), "today");
                dismiss();
            }
        });

        tom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTime.inventTime(tp.getCurrentHour(), tp.getCurrentMinute(), "tomorrow");
                dismiss();
            }
        });
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            sendTime = (SendTime) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");

        }
    }

    /*@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));

    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        h = i;
        m = i1;
        pickedTime();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            sendTime = (SendTime) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");

        }
    }

    public void pickedTime(){
        sendTime.inventTime(h,m);
    }*/
}
