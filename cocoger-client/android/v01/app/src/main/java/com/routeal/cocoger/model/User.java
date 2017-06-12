package com.routeal.cocoger.model;

import java.io.Serializable;

public class User implements Serializable
{

    private String email;
    private String password;
    private Device device;
    private final static long serialVersionUID = -8742997282931241705L;

    /**
     * No args constructor for use in serialization
     *
     */
    public User() {
    }

    /**
     *
     * @param email
     * @param device
     * @param password
     */
    public User(String email, String password, Device device) {
        super();
        this.email = email;
        this.password = password;
        this.device = device;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}
