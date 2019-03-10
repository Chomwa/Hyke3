package com.eafricar.hyke.Model;

import java.net.URI;

public class User {

    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String service;

    private URI resultUri;

    public User() {
        this.email = null;
        this.password = null;
        this.firstname = null;
        this.lastname = null;

    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) { this.firstname = firstName; }

    public void setLastName(String lastname) { this.lastname = lastname; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFirstName() {
        return firstname;
    }

    public String getLastName() { return lastname; }

    public String getPhoneNumber() { return phoneNumber; }

    public String toString() {return firstname + " " + lastname;}

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}

