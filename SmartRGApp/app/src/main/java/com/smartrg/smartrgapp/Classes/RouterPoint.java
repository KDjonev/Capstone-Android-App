package com.smartrg.smartrgapp.Classes;

import com.google.android.gms.maps.model.LatLng;

public class RouterPoint {

    private double longitude;
    private double latitude;
    private Integer rssi;

    public RouterPoint() {
    }

    public RouterPoint(double lat, double lon) {
        latitude = lat;
        longitude = lon;
        rssi = 0;
    }

    public void setLocation(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer r) {
        rssi = r;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}