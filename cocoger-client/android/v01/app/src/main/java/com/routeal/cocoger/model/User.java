package com.routeal.cocoger.model;

import java.io.Serializable;
import java.util.Map;

public class User implements Serializable {

    private String birthYear;
    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private String searchedName;
    private String gender;
    private String picture;
    private String locale;
    private String timezone;
    private long updated;
    private long created;
    private Map<String, String> devices;

    public User() {}

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchedName() {
        return searchedName;
    }

    public void setSearchedName(String searchedName) {
        this.searchedName = searchedName;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
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

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDevices(Map<String, String> devices) {
        this.devices = devices;
    }

    public Map<String, String> getDevices() {
        return devices;
    }

    public String toString() {
        return String.format(
                "email=%s, firstName=%s, lastName=%s, name=%s, locale=%s, picture=%s, " +
                        "birth_year=%s, gender=%s, timezone=%s, updated=%s",
                email, firstName, lastName, name, locale, picture,
                birthYear, gender, timezone, updated);
    }
}
