package com.routeal.cocoger.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by hwatanabe on 10/22/17.
 */

public class Group implements Serializable {

    private String name;
    private String color;
    private Map<String, Member> members;
    private long created;

    public Map<String, Member> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Member> members) {
        this.members = members;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
