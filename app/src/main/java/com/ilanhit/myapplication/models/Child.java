package com.ilanhit.myapplication.models;

public class Child extends Responsible{
    private boolean hasAlert;
    private double lat;
    private double lon;
    public Child(String id){
        super(id);
        hasAlert = false;
    }


    Child(){}
    public boolean isHasAlert() {
        return hasAlert;
    }

    public void setHasAlert(boolean hasAlert) {
        this.hasAlert = hasAlert;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
