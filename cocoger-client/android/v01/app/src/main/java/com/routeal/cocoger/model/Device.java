// generated from http://www.jsonschema2pojo.org/

package com.routeal.cocoger.model;

import java.io.Serializable;

public class Device implements Serializable {

    public final static int UNAVAILABLE = 0;
    public final static int BACKGROUND = 1;
    public final static int FOREGROUND = 2;

    private String uid;
    private String deviceId; // device unique id
    private String type = "mobile"; // device type - 'mobile', 'desktop', 'settop'
    private String platform = "android"; // platform - 'android', 'ios', 'windows'
    private String brand; // device brand - needed???
    private String model; // device model - needed???
    private String platformVersion; // system version
    private String appVersion; // app version
    private boolean simulator; // emulator or not
    private String token; // device token to be used for notification
    private int status; // unavailable, background, foreground
    private long timestamp; // last used

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean getSimulator() {
        return simulator;
    }

    public void setSimulator(boolean simulator) {
        this.simulator = simulator;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return String.format("deviceid=%s, type=%s, platform=%s, brand=%s, model=%s, " +
                        "platformVersion=%s, simulator=%s, token=%s appVersion=%s",
                deviceId, type, platform, brand, model,
                platformVersion, simulator, token, appVersion);
    }
}
