package com.routeal.cocoger.model;

import java.io.Serializable;

public class Friend implements Serializable {

    private int range;
    private long created;
    private String displayName;
    private String picture;
    private RangeRequest rangeRequest;
    private String location;

    public void setRange(int range) {
        this.range = range;
    }

    public int getRange() {
        return range;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getCreated() {
        return created;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() { return location; }

    public String getPicture() {
        return picture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setRangeRequest(RangeRequest request) {
        this.rangeRequest = request;
    }

    public RangeRequest getRangeRequest() {
        return rangeRequest;
    }
}
