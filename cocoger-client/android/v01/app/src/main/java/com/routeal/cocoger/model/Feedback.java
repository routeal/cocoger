package com.routeal.cocoger.model;

import java.io.Serializable;

public class Feedback implements Serializable {

    private int rating;
    private String title;
    private String description;
    private String id;
    private String name;
    private long created;

    public Feedback() {}

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) { this.id = id; }

    public void setName(String name) {this.name = name; }

    public void setCreated(long created) { this.created = created; }

    public int getRating() {
        return rating;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {return id; }

    public String getName() { return name; }

    public long getCreated() { return created; }
}
