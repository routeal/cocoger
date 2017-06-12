package com.routeal.cocoger.model;

import android.os.Build;

import com.routeal.cocoger.MainApplication;

import java.io.Serializable;

public class Device implements Serializable
{

    private String device;
    private String platform;
    private String platformVersion;
    private String country;
    private boolean simulator;
    private final static long serialVersionUID = -2638624042170848185L;

    /**
     * No args constructor for use in serialization
     *
     */
    public Device() {
    }

    /**
     *
     * @param platformVersion
     * @param platform
     * @param simulator
     * @param device
     * @param country
     */
    public Device(String device, String platform, String platformVersion, String country, boolean simulator) {
        super();
        this.device = device;
        this.platform = platform;
        this.platformVersion = platformVersion;
        this.country = country;
        this.simulator = simulator;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean getSimulator() {
        return simulator;
    }

    public void setSimulator(boolean simulator) {
        this.simulator = simulator;
    }

    public static Device getInstance() {
        String countryCode = MainApplication.getContext().getResources()
                .getConfiguration().locale.getCountry();
        Device device = new Device();
        device.setDevice(Build.DEVICE);
        device.setCountry(countryCode);
        device.setPlatformVersion(Build.VERSION.RELEASE);
        device.setPlatform(Build.VERSION.CODENAME);
        device.setSimulator(false);
        return device;
    }
}
