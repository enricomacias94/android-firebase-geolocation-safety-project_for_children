package com.ilanhit.myapplication.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ParentPerson extends Person<Child>{
    private Map<String, Child> contacts;
    ParentPerson(){
        contacts = new HashMap<>();
    };
    public ParentPerson(String firstName, String lastName, String phone, String email) {
        super(firstName, lastName, phone, email, PersonType.Parent);
        contacts = new HashMap<>();
    }
    @Override
    public Map<String, Child> getContacts() {
        return contacts;
    }

    @Override
    public void setContacts(Map<String, Child> contacts) {
        this.contacts = contacts;
    }
}
