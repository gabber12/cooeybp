package com.gabber12.cooey.cooey_bp.activity;

/**
 * Created by shubham.sharma on 26/12/17.
 */

public class Device {
    public String deviceId;
    public String deviceName;
    public String deviceLabel;
    public Device() {

        /// inject enum device type
    }
    public Device(String id, String name, String label) {
        this.deviceId = id;
        this.deviceName = name;
        this.deviceLabel = label;
    }

}
