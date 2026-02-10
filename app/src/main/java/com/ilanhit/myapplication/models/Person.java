package com.ilanhit.myapplication.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
public abstract class Person<T extends Responsible> implements Serializable {

    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private PersonType personType;
    private String id;

    public Person() {

    }

    public String getDetails(){
        return firstName + " " + lastName + " " + email;
    }
    public Person(String firstName, String lastName, String phone, String email, PersonType personType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.personType = personType;

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract Map<String, T> getContacts() ;

    public abstract void setContacts(Map<String, T> contacts) ;
    public String fullname(){
        return this.firstName + " " + this.lastName;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Person)) return false;
        Person other = (Person)obj;
        return this.id.equals(other.id);
    }
}
