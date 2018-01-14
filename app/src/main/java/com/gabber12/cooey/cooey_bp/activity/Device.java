package com.gabber12.cooey.cooey_bp.activity;

/**
 * Created by shubham.sharma on 26/12/17.
 */

public class Device {
    public String address;
    public String deviceName;
    public String deviceLabel;



    public String name;
   public int sensorType;
    public String pairFlags;
    private int key_id;
    private int rssi;
    private String scanRecord;
    private String modelNumber;


    public Device() {

        /// inject enum device type
    }
    public Device(String id, String name, String label) {
        this.address = id;
        this.deviceName = name;
        this.deviceLabel = label;
    }

}
