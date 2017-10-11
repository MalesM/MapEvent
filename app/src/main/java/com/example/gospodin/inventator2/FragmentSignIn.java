package com.example.gospodin.inventator2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSignIn extends Fragment implements View.OnClickListener {

    //SignInButton signInButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sign_in_fragment, container, false);

        /*signInButton = (SignInButton)v.findViewById(R.id.SgnInBtn);
        signInButton.setOnClickListener(this);*/

        return v;
    }

    @Override
    public void onClick(View v) {
        switch ( v.getId() )
        {
            case R.id.SgnInBtn:
                Log.d("aaaaa", "Radi");
                break;
        }
    }
}
