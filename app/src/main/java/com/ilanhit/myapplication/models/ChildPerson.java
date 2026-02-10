package com.ilanhit.myapplication.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChildPerson extends Person<Parent>{
    private Map<String, Parent> contacts;
    ChildPerson(){
        contacts = new HashMap<>();
    }


    public ChildPerson(String firstName, String lastName, String phone, String email) {
        super(firstName, lastName, phone, email, PersonType.Child);
        contacts = new HashMap<>();
    }

    @Override
    public Map<String, Parent> getContacts() {
        return contacts;
    }

    @Override
    public void setContacts(Map<String, Parent> contacts) {
        this.contacts = contacts;
    }
}
