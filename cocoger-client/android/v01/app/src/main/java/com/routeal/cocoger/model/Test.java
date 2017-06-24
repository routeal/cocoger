package com.routeal.cocoger.model;

import java.io.Serializable;

public class Test implements Serializable
{

    private String message;
    private final static long serialVersionUID = -7853824101840047248L;

    /**
     * No args constructor for use in serialization
     *
     */
    public Test() {
    }

    /**
     *
     * @param message
     */
    public Test(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
