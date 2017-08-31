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
    private String picture;
    private String locale;
    private String timezone;
    private long updated;
    private long created;
    private Map<String, String> devices;
    private Map<String, Friend> friends;
    private Map<String, Long> invites;
    private Map<String, Long> invitees;

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

    public Map<String, Friend> getFriends() {
        return friends;
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

    public String toString() {
        return String.format(
                "email=%s, firstName=%s, lastName=%s, displayName=%s, searchedName=%s, " +
                "gender=%s, locale=%s, timezone=%s, birth_year=%s, gender=%s, " +
                "timezone=%s, updated=%s picture=%s",
                email, firstName, lastName, displayName, searchedName, gender, locale,
                timezone, birthYear, gender, timezone, updated, picture);
    }
}
