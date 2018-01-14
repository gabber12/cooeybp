package com.gabber12.cooey.cooey_bp.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ankurshukla on 14/01/18.
 */

public class BpReadings {
    public int systolic;
    public int distolic;
    public int heartRate;

    public BpReadings(int systolic, int distolic, int heartRate) {
        this.systolic = systolic;
        this.distolic = distolic;
        this.heartRate = heartRate;
    }

}
