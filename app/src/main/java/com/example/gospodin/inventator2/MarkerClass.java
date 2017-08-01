package com.example.gospodin.inventator2;

enum Type {Sport, Culture, Party, Food}


public class MarkerClass {
    private double lat, lng;
    private String title="", description="";


    public MarkerClass(Double lat, Double lng, String title, String description){
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.description = description;
    }

    public double getLat(){
        return lat;
    }

    public double getLng(){
        return lng;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
