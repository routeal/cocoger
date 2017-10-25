package com.routeal.cocoger.model;

import java.io.Serializable;

/**
 * Created by hwatanabe on 10/22/17.
 */

public class Member implements Serializable {

    public final static int CREATED = 0;
    public final static int INVITED = 1;
    public final static int JOINED = 2;

    private int status;
    private long timestamp;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
