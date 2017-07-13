package com.routeal.cocoger.model;

import java.io.Serializable;

public class User implements Serializable {

    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private String gender;
    private String picture;
    private String locale;
    private String timezone;
    private String updated;
    private Device device;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String displayName) {
        this.name = displayName;
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

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String toString() {
        return String.format(
                "email=%s, firstName=%s, lastName=%s, name=%s, locale=%s, picture=%s, " +
                "gender=%s, timezone=%s, updated=%s, device=[%s]",
                email, firstName, lastName, name, locale, picture,
                gender, timezone, updated, ((device == null) ? "" : device.toString()));
    }
}
