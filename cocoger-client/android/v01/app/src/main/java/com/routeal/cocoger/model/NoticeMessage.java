package com.routeal.cocoger.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nabe on 9/22/17.
 */

public class NoticeMessage implements Serializable {
    private String title;
    private String message;
    private String picture;  // picture url
    private int resourceId; // local resource picture
    private String key; // user key (uid)
    private long id; // local database id
    private long created;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
