package com.gabber12.cooey.cooey_bp.service;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by shubham.sharma on 25/12/17.
 */

public class GattAttributes {

        private static HashMap<String, String> attributes = new HashMap();
        public static String SERVICE_UUID = "000018f0-0000-1000-8000-00805f9b34fb";
        public static String SERVICE_WRITE_CHANNEL = "00002af1-0000-1000-8000-00805f9b34fb";
        public static String SERVICE_READ_CHANNEL = "00002af0-0000-1000-8000-00805f9b34fb";


    public static final UUID WEIGHT_SCALE_APPEND_MEASUREMENT_UUID = UUID.fromString("00008A22-0000-1000-8000-00805f9b34fb");
    public static final UUID WEIGHT_SCALE_MEASUREMENT_UUID = UUID.fromString("00008A24-0000-1000-8000-00805f9b34fb");
    public static final UUID WEIGHT_SCALE_SERVICE_UUID = UUID.fromString("00007802-0000-1000-8000-00805f9b34fb");
    public static final UUID WEIGHT_SCLAE_APPEND_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00008a22-0000-1000-8000-00805f9b34fb");
    public static final UUID WEIGHT_SCLAE_FEATURE_CHARACTERISTIC_UUID = UUID.fromString("00008a20-0000-1000-8000-00805f9b34fb");
    public static final UUID WEIGHT_SCLAE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00008a21-0000-1000-8000-00805f9b34fb");

        public static byte[] hexStringToByteArray(String s) {
            int len = s.length();
            byte[] data = new byte[(len / 2)];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        }

        static {
            // Sample Services.
            attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
            attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
            // Sample Characteristics.
//        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
            attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        }

        public static String lookup(String uuid, String defaultName) {

            String name = attributes.get(uuid);
            return name == null ? defaultName : name;
        }

}
