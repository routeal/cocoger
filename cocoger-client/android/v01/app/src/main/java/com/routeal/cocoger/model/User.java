package com.routeal.cocoger.model;

import java.io.Serializable;
import java.util.Map;

public class User implements Serializable {

    private String birthYear;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private String searchedName;
    private String gender;
    private String locale;
    private String timezone;
    private String location;
    private long updated;
    private long created;
    private boolean test;
    private Map<String, String> devices;
    private Map<String, String> places;
    private Map<String, Long> invites;
    private Map<String, Long> invitees;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, String> getDevices() {
        return devices;
    }

    public void setDevices(Map<String, String> devices) {
        this.devices = devices;
    }

    public Map<String, Long> getInvites() {
        return invites;
    }

    public void setInvites(Map<String, Long> invites) {
        this.invites = invites;
    }

    public Map<String, Long> getInvitees() {
        return invitees;
    }

    public void setInvitees(Map<String, Long> invitees) {
        this.invitees = invitees;
    }

    public Map<String, String> getPlaces() {
        return places;
    }

    public boolean getTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public String toString() {
        return String.format(
                "email=%s, firstName=%s, lastName=%s, displayName=%s, searchedName=%s, " +
                        "gender=%s, locale=%s, timezone=%s, birth_year=%s, gender=%s, " +
                        "timezone=%s, updated=%s",
                email, firstName, lastName, displayName, searchedName, gender, locale,
                timezone, birthYear, gender, timezone, updated);
    }
}
