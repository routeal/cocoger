// generated from http://www.jsonschema2pojo.org/

package com.routeal.cocoger.model;

import java.io.Serializable;

public class Device implements Serializable {

    public final static int UNAVAILABLE = 0;
    public final static int BACKGROUND = 1;
    public final static int FOREGROUND = 2;

    private String id; // device unique id
    private String type = "mobile"; // device type - 'mobile', 'desktop', 'settop'
    private String platform = "android"; // platform - 'android', 'ios', 'windows'
    private String brand; // device brand - needed???
    private String model; // device model - needed???
    private String version; // system version
    private boolean simulator; // emulator or not
    private String token; // device token to be used for notification
    private int status; // unavailable, background, foreground

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean getSimulator() {
        return simulator;
    }

    public void setSimulator(boolean simulator) {
        this.simulator = simulator;
    }

    public void setToken(String token) { this.token = token; }

    public String getToken() { return token; }

    public String toString() {
        return String.format("id=%s, type=%s, platform=%s, brand=%s, model=%s, version=%s, simulator=%s, token=%s",
                             id, type, platform, brand, model, version, simulator, token);
    }
}
