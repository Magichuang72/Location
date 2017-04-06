package com.friendlyarm.SerialPortDemo;

/**
 * Created by huangbonan on 2016/8/24.
 */
public class Device_Rssi {
    private String id;
    private int rssi;
    private String name;

    Device_Rssi(String id, String name,int rssi) {
        this.id = id;
        this.rssi = rssi;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return this.id;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
