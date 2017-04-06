package com.smartrg.smartrgapp.Classes;

import com.google.android.gms.maps.model.LatLng;

public class TestPoint {

    private double latitude;
    private double longitude;
    private double intensity;
    private double upstream;
    private double downstream;
    private double jitter;
    private Integer retransmits;
    private double lostPercentage;
    private Integer rssi;

    public TestPoint() {
    }

    public TestPoint(double lat, double lon) {
        latitude = lat;
        longitude = lon;
        upstream = 0;
        downstream = 0;
        jitter = 0;
        retransmits = 0;
        lostPercentage = 0;
        rssi = 0;
    }

    public void setLocation(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double i) {
        intensity = i;
    }

    public double getLatitude() {
        return  latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setUpstream(double u) {
        upstream = u;
    }

    public void setDownstream(double d) {
        downstream = d;
    }

    public void setJitter(double j) {
        jitter = j;
    }

    public void setRetransmits(Integer i) {
        retransmits = i;
    }

    public void setLostPercentage(double l) {
        lostPercentage = l;
    }

    public void setRssi(Integer r) {
        rssi = r;
    }

    public Integer getRssi() {
        return rssi;
    }
}