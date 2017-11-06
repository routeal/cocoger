package com.routeal.cocoger.model;

import java.io.Serializable;

public class Friend implements Serializable {

    public final static int OFFLINE = 0;
    public final static int ONLINE = 1;

    private int range;
    private long created;
    private String displayName;
    private RangeRequest rangeRequest;
    private String location;
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

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
