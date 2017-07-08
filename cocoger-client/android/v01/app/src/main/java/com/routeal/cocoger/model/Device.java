// generated from http://www.jsonschema2pojo.org/

package com.routeal.cocoger.model;

import java.io.Serializable;

public class Device implements Serializable {

    private String id; // device unique id
    private String type; // device type - 'android', 'ios', 'windows'
    private String brand; // device brand - needed???
    private String model; // device model - needed???
    private String platform; // system
    private String platformVersion; // system version
    private String lang;  // user language
    private String country; // country - need more clarification
    private String simulator; // emulator or not
    private String token; // device token to be used for notification

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSimulator() {
        return simulator;
    }

    public void setSimulator(String simulator) {
        this.simulator = simulator;
    }

    public void setToken(String token) { this.token = token; }

    public String getToken() { return token; }
}
