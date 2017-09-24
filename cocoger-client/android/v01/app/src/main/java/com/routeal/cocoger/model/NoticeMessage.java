package com.routeal.cocoger.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nabe on 9/22/17.
 */

public class NoticeMessage implements Serializable {
    String title;
    String message;
    String picture;
    int resourceId;
    String key;
    long id;
    Date date;

    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setPicture(String picture) { this.picture = picture; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }
    public void setKey(String key) { this.key = key; }
    public void setDate(Date date) { this.date = date; }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getPicture() { return picture; }
    public int getResourceId() { return resourceId; }
    public String getKey() { return key; }
    public Date getDate() { return date; }
}
