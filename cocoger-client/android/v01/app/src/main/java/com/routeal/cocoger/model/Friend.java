package com.routeal.cocoger.model;

import java.io.Serializable;

public class Friend implements Serializable {

    private String providerId;
    private String firstName;
    private String lastName;
    private String name;
    private String gender;
    private String picture;
    private String locale;
    private String timezone;
    private String updated;
    private int status;
    private String approved;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getApproved() {
        return approved;
    }

    public void setApproved(String approved) {
        this.approved = approved;
    }

    public String toString() {
        return String.format(
                "email=%s, firstName=%s, lastName=%s, name=%s, locale=%s, picture=%s, " +
                        "gender=%s, timezone=%s, updated=%s, device=[%s]",
                providerId, firstName, lastName, name, locale, picture,
                gender, timezone, updated);
    }
}
