package com.gabber12.cooey.cooey_bp.activity;

import com.gabber12.cooey.cooey_bp.DeviceType;

/**
 * Created by shubham.sharma on 26/12/17.
 */

public class Device {
    public String deviceId;
    public String deviceName;
    public DeviceType deviceType;
    public Device() {

        /// inject enum device type
    }
    public Device(String id, String name) {
        this.deviceId = id;
        this.deviceName = name;
    }

}
