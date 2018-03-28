package com.gabber12.cooey.cooey_bp.activity

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.TextView
import com.gabber12.cooey.cooey_bp.QrCodeScannerActivity
import com.gabber12.cooey.cooey_bp.R
import com.gabber12.cooey.cooey_bp.service.BluetoothLeService
import com.gabber12.cooey.cooey_bp.service.BpReadings
import com.gabber12.cooey.cooey_bp.service.GattAttributes
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_device.*
import java.util.*

class IhealthOximeter : AppCompatActivity() {


    private var mBluetoothLeService: BluetoothLeService? = null
    private var mDeviceAddress: String? = null
    private var mConnected: Boolean = false
    private var  isCompleted: Boolean = false;
    private var finalReadings: BpReadings? = null
//    private var mSeries2: LineGraphSeries<DataPointInterface> = LineGraphSeries()
//    private var mSeries3: LineGraphSeries<DataPointInterface> = LineGraphSeries()
//    private var mSeries4: LineGraphSeries<DataPointInterface> = LineGraphSeries()


    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var progress1: Int = 0;
    private var mGattUpdateReceiver: BroadcastReceiver = (object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.getAction()
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                this@IhealthOximeter.mConnected = true
                this@IhealthOximeter.updateConnectionState(R.string.connected)
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                this@IhealthOximeter.mConnected = false
                this@IhealthOximeter.updateConnectionState(R.string.disconnected)
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                this@IhealthOximeter.displayGattServices(this@IhealthOximeter.mBluetoothLeService?.getSupportedGattServices())
                this@IhealthOximeter.updateConnectionStateForButton(R.string.start)
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE != action) {
            } else {

                if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS) != null) {
                    Log.i("Device", intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS))
                    this@IhealthOximeter.updateDataBatteryStatus(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS))
                } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_SYSTOLIC_PROGRESS) != null) {
                    this@IhealthOximeter.updateDataSystolicValues(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_SYSTOLIC_PROGRESS))
                } else if (intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_SYSTOLIC, 0) !== 0 && intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_DIASTOLIC, 0) !== 0 && intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_HEART_RATE, 0) !== 0) {
                    this@IhealthOximeter.updateBPValues(intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_SYSTOLIC, 0), intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_DIASTOLIC, 0), intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_HEART_RATE, 0))
                    isCompleted = true
                } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ERROR) != null) {
                    this@IhealthOximeter.updateErrorDetails(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ERROR))
                }
            }
        }

    })
    private val mServiceConnection = (object: ServiceConnection {


        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            this@IhealthOximeter.mBluetoothLeService = (service as BluetoothLeService.LocalBinder)?.getService();
            if (!this@IhealthOximeter.mBluetoothLeService!!.initialize()) {

                this@IhealthOximeter.finish();
            }
            this@IhealthOximeter.mBluetoothLeService!!.connect(this@IhealthOximeter.mDeviceAddress);
        }


        override fun onServiceDisconnected( componentName: ComponentName) {
            this@IhealthOximeter.mBluetoothLeService = null;
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ihealth_oximeter)

        mDeviceAddress  = intent.getStringExtra("deviceId")

        bindService(Intent(this, BluetoothLeService::class.java), this.mServiceConnection, Context.BIND_AUTO_CREATE)
        registerReceiver(this.mGattUpdateReceiver, makeGattUpdateIntentFilter());

//        for (gattService in this@IhealthOximeter?.mBluetoothLeService?.getSupportedGattServices()!!) {
//            if (gattService.getUuid().toString() == GattAttributes.SERVICE_UUID) {
//                for (gattCharacteristic in gattService.getCharacteristics()) {
//                    this@IhealthOximeter.mBluetoothLeService?.readCharacteristic(gattCharacteristic)
//
//                }
//            }
//        }
        val random = Random()
        var newReading = (random.nextInt(50) + 55);

        startMeasurement.setOnClickListener((object: View.OnClickListener {
            override fun onClick(p0: View?) {
                if( isCompleted) {
                    val intent = Intent(applicationContext, QrCodeScannerActivity::class.java)
                    intent.putExtra("SYSTOLIC",newReading)

//                    intent.putExtra("SYSTOLIC",1 )
//                    intent.putExtra("DISTOLIC",1 )
//                    intent.putExtra("HEART_RATE",1 )
                    startActivity(intent)
                    return
                }

                hrText.setText(""+newReading)
                current_reading.setText(""+newReading)

                findViewById<TextView>(R.id.startMeasurement).setText("Upload Measurements")
                isCompleted = true


            }

        }))
    }



    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiver)
    }

    fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        return intentFilter;
    }

    private fun updateConnectionStateForButton(start: Int) {
        //Set Text
        Log.i("Device", ""+start);
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices != null) {
            for (gattService in gattServices) {
                if (gattService.uuid.toString() == GattAttributes.SERVICE_UUID) {
                    for (gattCharacteristic in gattService.characteristics) {
                        if (gattCharacteristic.uuid.toString() == GattAttributes.SERVICE_READ_CHANNEL) {
                            displayData(gattCharacteristic)
                        }
                    }
                }
            }
        }
    }


    private fun displayData(bluetoothGattCharacteristic: BluetoothGattCharacteristic) {
        val charaProp = bluetoothGattCharacteristic.properties
        if (charaProp or 2 > 0) {
            if (this.mNotifyCharacteristic != null) {
                this.mBluetoothLeService?.setCharacteristicNotification(this.mNotifyCharacteristic, false)
                this.mNotifyCharacteristic = null
            }
            this.mBluetoothLeService?.readCharacteristic(bluetoothGattCharacteristic)
        }
        if (charaProp or 16 > 0) {
            this.mNotifyCharacteristic = bluetoothGattCharacteristic
            this.mBluetoothLeService?.setCharacteristicNotification(bluetoothGattCharacteristic, true)
        }
    }

    private fun updateDataBatteryStatus(data: String?) {
        if (data != null) {
            progressBattery.progress = Integer.parseInt(data)
            progressText.setText(getPercentage(data));
        } else {
//            this.txtDevieBatteryStatus.setText("-")
        }
    }
    private fun getPercentage(data: String?): String {
        return data +" %"
    }

    private fun updateDataSystolicValues(stringExtra: String?) {

        if (stringExtra != null) {
            val currentReadingText = findViewById<TextView>(R.id.current_reading)
            currentReadingText.setText(stringExtra)
//            mSeries2.appendData(DataPoint(progress1*1.0, Integer.parseInt(stringExtra)*1.0), false , 100)
//            progress1 ++;

//            this.txtProgressStatus.setText(stringExtra)
//            this.button.setText("Stop")
//            return
        }
//        this.txtProgressStatus.setText("-")
    }

    private fun updateBPValues(systolicValue: Int, diastolicValue: Int, heartRateValue: Int) {


//        this.systolic = systolicValue
//        this.dystolic = diastolicValue
//        this.heartRate = heartRateValue
//        this.txtProgressStatus.setText(systolicValue.toString() + "/" + diastolicValue + "mmHg")
        finalReadings = BpReadings(systolicValue, diastolicValue, heartRateValue)
        findViewById<TextView>(R.id.startMeasurement).setText("Upload Measurements")
//        systolicText.setText(""+systolicValue)
//        distolicText.setText(""+diastolicValue)
        hrText.setText(""+heartRateValue)
        startMeasurement.setEnabled(true);
        checkBPRange(systolicValue, diastolicValue)
    }

    private fun updateErrorDetails(stringExtra: String?) {
        if (stringExtra != null) {

//            this.txtProgressStatus.setText("Error Occured" + stringExtra)
//            if (this.mConnected) {
//                this.button.setText(getString(R.string.start))
//            } else {
//                this.button.setText(getString(R.string.connect))
//            }
        }
    }

    private fun updateConnectionState(resourceId: Int) {
        runOnUiThread {
            if (resourceId == R.string.connected) {
                this@IhealthOximeter.progressBar.visibility = View.GONE
            }
            this@IhealthOximeter.connectionStatus.setText(resourceId)
        }
    }
    private fun checkBPRange(systolic: Int, diastolic: Int) {
        var bpRangeTosend = 0
        if (systolic < 90 && diastolic < 60) {
            bpRangeTosend = 1
        } else if (systolic < 90 && diastolic >= 60 && diastolic < 90) {
            bpRangeTosend = 1
        } else if (systolic < 90 && diastolic >= 90) {
            bpRangeTosend = 4
        } else if (systolic >= 90 && systolic < 140 && diastolic >= 90) {
            bpRangeTosend = 3
        } else if (systolic >= 90 && systolic < 140 && diastolic >= 60 && diastolic < 90) {
            bpRangeTosend = 2
        } else if (systolic >= 90 && systolic < 140 && diastolic < 60) {
            bpRangeTosend = 1
        } else if (systolic >= 140 && diastolic >= 90) {
            bpRangeTosend = 3
        } else if (systolic >= 140 && diastolic >= 60 && diastolic < 90) {
            bpRangeTosend = 3
        } else if (systolic >= 140 && diastolic < 60) {
            bpRangeTosend = 5
        }
        setRangeOnUI(bpRangeTosend)
    }

    fun setRangeOnUI(bpRange: Int) {
        if (bpRange > 0 && bpRange <= 5) {
//            if (this.txtBpRange.getVisibility() === View.GONE) {
//                this.txtBpRange.setVisibility(View.VISIBLE)
//            }
//            when (bpRange) {
//                1 -> {
//                    this.txtBpRange.setText("Your Blood Pressure is Low")
//                    return
//                }
//                2 -> {
//                    this.txtBpRange.setText("Your Blood Pressure is Normal")
//                    return
//                }
//                3 -> {
//                    this.txtBpRange.setText("Your Blood Pressure is High")
//                    return
//                }
//                4 -> {
//                    this.txtBpRange.setText("Your Blood Pressure is Low/High")
//                    return
//                }
//                5 -> {
//                    this.txtBpRange.setText("Your Blood Pressure is High/Low")
//                    return
//                }
//                else -> return
//            }
        }
    }
}
