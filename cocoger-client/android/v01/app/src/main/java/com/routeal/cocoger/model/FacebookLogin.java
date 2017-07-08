package com.routeal.cocoger.model;

import java.io.Serializable;

public class FacebookLogin implements Serializable {

    private String authToken;
    private Device device;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}
