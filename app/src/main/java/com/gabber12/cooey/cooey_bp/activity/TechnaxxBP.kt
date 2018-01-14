package com.gabber12.cooey.cooey_bp.activity

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.gabber12.cooey.cooey_bp.R
import com.gabber12.cooey.cooey_bp.service.BluetoothLeService
import android.content.IntentFilter
import android.util.Log
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.gabber12.cooey.cooey_bp.service.GattAttributes
import android.graphics.Color
import android.view.View
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import kotlinx.android.synthetic.main.activity_device.*
import com.jjoe64.graphview.series.LineGraphSeries
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.widget.TextView
import com.gabber12.cooey.cooey_bp.QrCodeScannerActivity
import com.gabber12.cooey.cooey_bp.service.BpReadings
import com.jjoe64.graphview.GridLabelRenderer


class TechnaxxBP : AppCompatActivity() {
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
                this@TechnaxxBP.mConnected = true
                this@TechnaxxBP.updateConnectionState(R.string.connected)
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                this@TechnaxxBP.mConnected = false
                this@TechnaxxBP.updateConnectionState(R.string.disconnected)
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                this@TechnaxxBP.displayGattServices(this@TechnaxxBP.mBluetoothLeService?.getSupportedGattServices())
                this@TechnaxxBP.updateConnectionStateForButton(R.string.start)
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE != action) {
            } else {

                if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS) != null) {
                    Log.i("Device", intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS))
                    this@TechnaxxBP.updateDataBatteryStatus(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS))
                } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_SYSTOLIC_PROGRESS) != null) {
                    this@TechnaxxBP.updateDataSystolicValues(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_SYSTOLIC_PROGRESS))
                } else if (intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_SYSTOLIC, 0) !== 0 && intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_DIASTOLIC, 0) !== 0 && intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_HEART_RATE, 0) !== 0) {
                    this@TechnaxxBP.updateBPValues(intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_SYSTOLIC, 0), intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_DIASTOLIC, 0), intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_HEART_RATE, 0))
                    isCompleted = true
                } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ERROR) != null) {
                    this@TechnaxxBP.updateErrorDetails(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ERROR))
                }
            }
        }

    })
    private val mServiceConnection = (object: ServiceConnection {


        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            this@TechnaxxBP.mBluetoothLeService = (service as BluetoothLeService.LocalBinder)?.getService();
            if (!this@TechnaxxBP.mBluetoothLeService!!.initialize()) {

                this@TechnaxxBP.finish();
            }
            this@TechnaxxBP.mBluetoothLeService!!.connect(this@TechnaxxBP.mDeviceAddress);
        }


        override fun onServiceDisconnected( componentName: ComponentName) {
            this@TechnaxxBP.mBluetoothLeService = null;
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        mDeviceAddress  = intent.getStringExtra("deviceId")
        bindService(Intent(this, BluetoothLeService::class.java), this.mServiceConnection, Context.BIND_AUTO_CREATE)
        registerReceiver(this.mGattUpdateReceiver, makeGattUpdateIntentFilter());

//        graph.setTitle("Measurement")
//
//        graph.pivotY = 0F
//        graph.pivotX = 0F
//        val viewport = graph.viewport
//        viewport.setMaxX(80.0)
//        viewport.setMinX(0.0)
//        viewport.setMinY(0.0)
//        viewport.setMaxY(200.0)
//        viewport.isXAxisBoundsManual = true
//        viewport.isYAxisBoundsManual = true
//
//        graph.gridLabelRenderer.isHorizontalLabelsVisible = false
//        graph.gridLabelRenderer.gridColor = R.color.switch_thumb_material_light
//        graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE
//
//        prepareSeries(mSeries2, Color.GRAY)
//        graph.addSeries(mSeries2)

//        prepareSeries(mSeries3, Color.BLACK);
//        graph.addSeries(mSeries3)
//        prepareSeries(mSeries4, Color.LTGRAY);
//        graph.addSeries(mSeries4)

        startMeasurement.setOnClickListener((object: View.OnClickListener {
            override fun onClick(p0: View?) {
                if( isCompleted) {
                    val intent = Intent(applicationContext, QrCodeScannerActivity::class.java)
                    intent.putExtra("SYSTOLIC",finalReadings?.systolic )
                    intent.putExtra("DISTOLIC",finalReadings?.distolic )
                    intent.putExtra("HEART_RATE",finalReadings?.heartRate )

//                    intent.putExtra("SYSTOLIC",1 )
//                    intent.putExtra("DISTOLIC",1 )
//                    intent.putExtra("HEART_RATE",1 )
                    startActivity(intent)
                    return;
                }
                startMeasurement.setEnabled(false);
                for (gattService in this@TechnaxxBP?.mBluetoothLeService?.getSupportedGattServices()!!) {
                    if (gattService.getUuid().toString() == GattAttributes.SERVICE_UUID) {
                        for (gattCharacteristic in gattService.getCharacteristics()) {
                            if (gattCharacteristic.getUuid().toString() == GattAttributes.SERVICE_WRITE_CHANNEL) {
                                val isValueWritten = gattCharacteristic.setValue(GattAttributes.hexStringToByteArray("0x0240dc01a13c"))
                                this@TechnaxxBP.mBluetoothLeService?.writeCharacteristic(gattCharacteristic)
                            }
                        }
                    }
                }
            }

        }))
    }

    private fun prepareSeries(mSeries2: LineGraphSeries<DataPointInterface>, color: Int) {
        mSeries2.color = color
        mSeries2.setDrawDataPoints(true)
        mSeries2.setDataPointsRadius(10F)
        mSeries2.setThickness(8)
        mSeries2.setAnimated(true)
        val paint = Paint()
        paint.setStyle(Paint.Style.STROKE)
        paint.color = Color.LTGRAY
        paint.setStrokeWidth(8F)
        paint.setPathEffect(DashPathEffect(floatArrayOf(5f, 5f), 0f))
        mSeries2.setCustomPaint(paint)
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

    private fun updateConnectionState(resourceId: Int) {
        runOnUiThread {
            if (resourceId == R.string.connected) {
                this@TechnaxxBP.progressBar.visibility = View.GONE
            }
            this@TechnaxxBP.connectionStatus.setText(resourceId)
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
        systolicText.setText(""+systolicValue)
        distolicText.setText(""+diastolicValue)
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
