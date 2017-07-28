package com.example.gospodin.inventator2;

import com.google.android.gms.maps.model.Marker;

enum Type {Sport, Culture, Party};


public class MarkerClass {
    private Marker marker;
    private String title, description;
    private Type type;

    public MarkerClass(Marker marker, String title, String description, Type type){
        this.marker = marker;
        this.title = title;
        this.description = description;
        this.type = type;
    }


}
