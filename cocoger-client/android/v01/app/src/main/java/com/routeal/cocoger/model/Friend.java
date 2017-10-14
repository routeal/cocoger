package com.routeal.cocoger.model;

import java.io.Serializable;

public class Friend implements Serializable {

    private int range;
    private long created;
    private String displayName;
    private RangeRequest rangeRequest;
    private String location;

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public RangeRequest getRangeRequest() {
        return rangeRequest;
    }

    public void setRangeRequest(RangeRequest request) {
        this.rangeRequest = request;
    }
}
