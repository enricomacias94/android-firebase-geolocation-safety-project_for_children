package com.ilanhit.myapplication.models;

import java.io.Serializable;

public class Responsible implements Serializable {
    private String id;

    public Responsible(){}
    public Responsible(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
