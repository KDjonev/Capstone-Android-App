package com.smartrg.smartrgapp.Classes;

/**
 * Created by root on 5/4/17.
 */

public class Device {

    private String ip, mac, name;

    public Device(String n, String i, String m) {
        name = n;
        ip = i;
        mac = m;
    }

    public String getIp() {
        return ip;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setName(String name) {
        this.name = name;
    }
}
