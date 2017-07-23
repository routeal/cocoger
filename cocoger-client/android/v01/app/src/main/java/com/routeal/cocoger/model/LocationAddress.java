// http://www.jsonschema2pojo.org/

package com.routeal.cocoger.model;

import java.io.Serializable;

public class LocationAddress implements Serializable {

    private long id;
    private long time;
    private double latitude;
    private double longitude;
    private double altitude;
    private float speed;
    private String postalCode;
    private String countryName;
    private String adminArea;
    private String subAdminArea;
    private String locality;
    private String subLocality;
    private String thoroughfare;
    private String subThoroughfare;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getAdminArea() {
        return adminArea;
    }

    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }

    public String getSubAdminArea() {
        return subAdminArea;
    }

    public void setSubAdminArea(String subAdminArea) {
        this.subAdminArea = subAdminArea;
    }

    public String getlocality() {
        return locality;
    }

    public void setlocality(String locality) {
        this.locality = locality;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public void setSubLocality(String subLocality) {
        this.subLocality = subLocality;
    }

    public String getThoroughfare() {
        return thoroughfare;
    }

    public void setThoroughfare(String thoroughfare) {
        this.thoroughfare = thoroughfare;
    }

    public String getSubThoroughfare() {
        return subThoroughfare;
    }

    public void setSubThoroughfare(String subThoroughfare) {
        this.subThoroughfare = subThoroughfare;
    }
}
