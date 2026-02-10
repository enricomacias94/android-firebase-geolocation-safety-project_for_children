package com.ilanhit.myapplication.models;

public class Parent extends Responsible {

    private boolean enabled;
    private boolean requestLocation;

    public Parent(){}
    public Parent(String id){
        super(id);
        this.enabled = true;
        requestLocation = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRequestLocation() {
        return requestLocation;
    }

    public void setRequestLocation(boolean requestLocation) {
        this.requestLocation = requestLocation;
    }
}
