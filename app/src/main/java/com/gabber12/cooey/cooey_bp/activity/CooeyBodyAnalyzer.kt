//package com.gabber12.cooey.cooey_bp.activity
//
//import android.bluetooth.BluetoothGattCharacteristic
//import android.bluetooth.BluetoothGattService
//import android.content.*
//import android.graphics.Color
//import android.graphics.DashPathEffect
//import android.graphics.Paint
//import android.os.Bundle
//import android.os.Handler
//import android.os.IBinder
//import android.support.v7.app.AppCompatActivity
//import android.util.JsonReader
//import android.util.JsonWriter
//import android.util.Log
//import android.view.View
//import com.gabber12.cooey.cooey_bp.R
//import com.gabber12.cooey.cooey_bp.service.BleDevice
//import com.gabber12.cooey.cooey_bp.service.BluetoothLeService
//import com.gabber12.cooey.cooey_bp.service.GattAttributes
//import com.google.gson.Gson
//import com.jjoe64.graphview.series.DataPoint
//import com.jjoe64.graphview.series.DataPointInterface
//import com.jjoe64.graphview.series.LineGraphSeries
//import kotlinx.android.synthetic.main.activity_device.*
//import kotlinx.android.synthetic.main.list_single.*
//import com.lifesense.ble.bean.*
//import com.lifesense.ble.commom.BleDeviceManager
//import com.lifesense.ble.commom.DeviceManagerCallback
//import org.json.JSONObject
//
//class CooeyBodyAnalyzer : AppCompatActivity() {
//    private var mBluetoothLeService: BluetoothLeService? = null
//    private var mDeviceAddress: String? = null
//    private var mConnected: Boolean = false
//    private var mSeries2: LineGraphSeries<DataPointInterface> = LineGraphSeries()
//    private var mSeries3: LineGraphSeries<DataPointInterface> = LineGraphSeries()
//    private var mSeries4: LineGraphSeries<DataPointInterface> = LineGraphSeries()
//    private var mHandler = Handler()
//    private var sharePrefs: SharedPreferences? = null
//    private var bleDeviceManager: BleDeviceManager? = null;
//    private var pairedDevice: LSDeviceInfo? = null
//    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
//    private var progress1: Int = 0;
//    private var mGattUpdateReceiver: BroadcastReceiver = (object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val action = intent?.getAction()
//            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
//                this@CooeyBodyAnalyzer.mConnected = true
//                this@CooeyBodyAnalyzer.updateConnectionState(R.string.connected)
//
//                Log.i("Device", "Our new device is now connected ¡¡˚¬¬˚˚asåßΩ≈∂ƒç©©h˘")
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
//                this@CooeyBodyAnalyzer.mConnected = false
//                this@CooeyBodyAnalyzer.updateConnectionState(R.string.disconnected)
//            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
//                this@CooeyBodyAnalyzer.displayGattServices(this@CooeyBodyAnalyzer.mBluetoothLeService?.getSupportedGattServices())
//                this@CooeyBodyAnalyzer.updateConnectionStateForButton(R.string.start)
//            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE != action) {
//            } else {
//
//                if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS) != null) {
//                    Log.i("Device", intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS))
//                    this@CooeyBodyAnalyzer.updateDataBatteryStatus(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_BATTERY_STATUS))
//                } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_SYSTOLIC_PROGRESS) != null) {
//                    this@CooeyBodyAnalyzer.updateDataSystolicValues(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_SYSTOLIC_PROGRESS))
//                } else if (intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_SYSTOLIC, 0) !== 0 && intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_DIASTOLIC, 0) !== 0 && intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_HEART_RATE, 0) !== 0) {
//                    this@CooeyBodyAnalyzer.updateBPValues(intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_SYSTOLIC, 0), intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_DIASTOLIC, 0), intent.getIntExtra(BluetoothLeService.EXTRA_DATA_BP_HEART_RATE, 0))
//                } else if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ERROR) != null) {
//                    this@CooeyBodyAnalyzer.updateErrorDetails(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ERROR))
//                }
//            }
//        }
//
//    })
//    private val mServiceConnection = (object : ServiceConnection {
//
//
//        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
//            this@CooeyBodyAnalyzer.mBluetoothLeService = (service as BluetoothLeService.LocalBinder)?.getService();
//            if (!this@CooeyBodyAnalyzer.mBluetoothLeService!!.initialize()) {
//
//                this@CooeyBodyAnalyzer.finish();
//            }
//            this@CooeyBodyAnalyzer.mBluetoothLeService!!.connect(this@CooeyBodyAnalyzer.mDeviceAddress);
//        }
//
//
//        override fun onServiceDisconnected(componentName: ComponentName) {
//            this@CooeyBodyAnalyzer.mBluetoothLeService = null;
//        }
//    })
//
//    fun startScanning(bodyMassDeviceAddress: String?) {
//        if (pairedDevice != null) {
//            readMeasurement(pairedDevice)
//            return;
//        }
//
//        if (bleDeviceManager != null) {
//            val manager = bleDeviceManager
//            val callback = (object : DeviceManagerCallback() {
//                override fun onDiscoverDevice(p0: LSDeviceInfo?) {
//                    super.onDiscoverDevice(p0)
//                    if (p0?.deviceAddress.equals(bodyMassDeviceAddress)) {
//                        Log.i("TAG", "FOUND DEVICE")
//                        if (p0 != null) {
//                            val editor = sharePrefs?.edit()
//                            val gson = Gson()
//
//                            editor?.putString("PAIRED_DEVICE", gson.toJson(p0))
//                            editor?.commit()
//                            pairDevice(p0)
//                        }
//                    }
//                }
//            })
//            manager?.initialize(this.applicationContext, callback)
//            manager?.setCallback(callback)
//            if (manager?.startScanning()!!) {
//                val stopScanner = (object : Runnable {
//                    override fun run() {
//                        manager.stopScanning()
//                    }
//
//                })
//                mHandler.postDelayed(stopScanner, 10000)
//
////                bleDeviceManager?.stopScanning()
//                Log.i("TAG", "PAIR NOW")
//            }
//
//        }
//
//
//    }
//
//    fun readMeasurement(p0: LSDeviceInfo?) {
//        if (p0 == null) return
//        bleDeviceManager?.interruptCurrentTask()
//        bleDeviceManager?.setCallback((object : DeviceManagerCallback() {
//            override fun onReceivePedometerMeasurementData(p0: PedometerData?) {
//                super.onReceivePedometerMeasurementData(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onReceiveDataFinish(p0: Int) {
//                super.onReceiveDataFinish(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onReceiveKitchenScaleMeasurementData(p0: KitchenScaleData?) {
//                super.onReceiveKitchenScaleMeasurementData(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onGlucoseDeviceInfo(p0: GlucoseDeviceInfo?) {
//                super.onGlucoseDeviceInfo(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onDiscoverBeneCheckDevice(p0: GlucoseDeviceInfo?) {
//                super.onDiscoverBeneCheckDevice(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onReceiveGlucoseMeasurementData(p0: GlucoseData?) {
//                super.onReceiveGlucoseMeasurementData(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onReceiveHeightMeasurementData(p0: HeightData?) {
//                super.onReceiveHeightMeasurementData(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onPairedResults(p0: LSDeviceInfo?, p1: Int) {
//                super.onPairedResults(p0, p1)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onReceiveBloodPressureMeasurementData(p0: BloodPressureData?) {
//                super.onReceiveBloodPressureMeasurementData(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onDiscoverDevice(p0: LSDeviceInfo?) {
//                super.onDiscoverDevice(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//            override fun onReceiveWeightMeasurementData(p0: WeightData?) {
//                super.onReceiveWeightMeasurementData(p0)
//                Log.i("TAG", "FOUND DATA");
//            }
//
//        }))
//        bleDeviceManager?.getDeviceMeasurementData(p0)
//    }
//
//    fun pairDevice(p0: LSDeviceInfo) {
//        val callback = (object : DeviceManagerCallback() {
//            override fun onPairedResults(p0: LSDeviceInfo?, p1: Int) {
//                super.onPairedResults(p0, p1)
//                Log.i("TAG", "PAIRED")
//                readMeasurement(p0)
//            }
//        })
//        bleDeviceManager?.setCallback(callback)
//        bleDeviceManager?.toPairDevice(p0)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_bodyanalyzer_device)
//        sharePrefs = getSharedPreferences(packageName, Context.MODE_PRIVATE)
//        bleDeviceManager = BleDeviceManager.getInstance()
//        val pairedDeviceString = sharePrefs?.getString("PAIRED_DEVICE", null)
//        if (pairedDeviceString != null) {
//            pairedDevice = Gson().fromJson(pairedDeviceString, LSDeviceInfo::class.java)
//        }
//
//        mDeviceAddress = intent.getStringExtra("address")
//        if (mDeviceAddress != null)
//            startScanning(mDeviceAddress)
//
////        bindService(Intent(this, BluetoothLeService::class.java), this.mServiceConnection, Context.BIND_AUTO_CREATE)
////        registerReceiver(this.mGattUpdateReceiver, makeGattUpdateIntentFilter());
//
////        graph.setTitle("Measurement")
////
////        graph.pivotY = 0F
////        graph.pivotX = 0F
////        val viewport = graph.viewport
////        viewport.setMaxX(80.0)
////        viewport.setMinX(0.0)
////        viewport.setMinY(0.0)
////        viewport.setMaxY(200.0)
////        viewport.isXAxisBoundsManual = true
////        viewport.isYAxisBoundsManual = true
//
////        graph.gridLabelRenderer.isHorizontalLabelsVisible = false
////        graph.gridLabelRenderer.gridColor = R.color.switch_thumb_material_light
////        graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE
////
////        prepareSeries(mSeries2, Color.GRAY)
////        graph.addSeries(mSeries2)
//
////        prepareSeries(mSeries3, Color.BLACK);
////        graph.addSeries(mSeries3)
////        prepareSeries(mSeries4, Color.LTGRAY);
////        graph.addSeries(mSeries4)
//
//        startMeasurement.setOnClickListener((object : View.OnClickListener {
//            override fun onClick(p0: View?) {
//                for (gattService in this@CooeyBodyAnalyzer?.mBluetoothLeService?.getSupportedGattServices()!!) {
//                    if (gattService.getUuid().toString() == GattAttributes.SERVICE_UUID) {
//                        for (gattCharacteristic in gattService.getCharacteristics()) {
//                            if (gattCharacteristic.getUuid().toString() == GattAttributes.SERVICE_WRITE_CHANNEL) {
//                                val isValueWritten = gattCharacteristic.setValue(GattAttributes.hexStringToByteArray("0x0240dc01a13c"))
//                                this@CooeyBodyAnalyzer.mBluetoothLeService?.writeCharacteristic(gattCharacteristic)
//                            }
//                        }
//                    }
//                }
//            }
//
//        }))
//    }
//
////    private fun pairDevice(jsonString: String) {
////        try {
////            val json = JSONObject(jsonString)
////            val device = gson.fromJson(json.get("device").toString(), BleDevice::class.java)
////            val timeout = json.get("timeout") as Int
////            Log.d(tag, "param:" + jsonString)
////            Log.d(tag, "Pair device:" + device.toString())
////            val mDevice = LSDeviceInfo()
////            mDevice.deviceName = device.getName()
////            mDevice.deviceType = typeConversion.integerToEnum(device.getSensorType())
////            mDevice.deviceAddress = device.getAddress()
////            mDevice.modelNumber = device.getModelNumber()
////            bleDeviceManager.stopScanning()
////            pairInterruptFlag = true
////            mDelegate = object : DeviceManagerCallback() {
////                override fun onPairedResults(device: LSDeviceInfo?, state: Int) {
////                    if (device != null && state == 0) {
////                        pairedDevice = device
////                        val editor = sharedPref.edit()
////                        editor.putString(device.deviceAddress, gson.toJson(device))
////                        editor.commit()
////                        Log.d(tag, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Device paired:" + device.deviceName)
////                        pairInterruptFlag = false
////                        getPairedDevice(callbackContext)
////                    }
////                }
////            }
////            bleDeviceManager.setCallback(mDelegate)
////            if (!bleDeviceManager.toPairDevice(mDevice)) {
////                callbackContext.error(RUNNING_TASKS_ERROR)
////            } else {
////                val myTask = Runnable {
////                    if (pairInterruptFlag) {
////                        Log.d(tag, "InterruptResult:" + bleDeviceManager.interruptCurrentTask())
////                    }
////                    callbackContext.error("Pair Time Out.")
////                }
////                mHandler.postDelayed(myTask, timeout.toLong())
////            }
////        } catch (e: Exception) {
////            e.printStackTrace()
////        }
//
//    //    }
//    private fun prepareSeries(mSeries2: LineGraphSeries<DataPointInterface>, color: Int) {
//        mSeries2.color = color
//        mSeries2.setDrawDataPoints(true)
//        mSeries2.setDataPointsRadius(10F)
//        mSeries2.setThickness(8)
//        mSeries2.setAnimated(true)
//        val paint = Paint()
//        paint.setStyle(Paint.Style.STROKE)
//        paint.color = Color.LTGRAY
//        paint.setStrokeWidth(8F)
//        paint.setPathEffect(DashPathEffect(floatArrayOf(5f, 5f), 0f))
//        mSeries2.setCustomPaint(paint)
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//    }
//
//    fun makeGattUpdateIntentFilter(): IntentFilter {
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
//        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
//        return intentFilter;
//    }
//
//    private fun updateConnectionStateForButton(start: Int) {
//        //Set Text
//        Log.i("Device", "" + start);
//    }
//
//    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
//        if (gattServices != null) {
//            for (gattService in gattServices) {
//                if (gattService.uuid.toString() == GattAttributes.SERVICE_UUID) {
//                    for (gattCharacteristic in gattService.characteristics) {
//                        if (gattCharacteristic.uuid.toString() == GattAttributes.SERVICE_READ_CHANNEL) {
//                            displayData(gattCharacteristic)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun updateConnectionState(resourceId: Int) {
//        runOnUiThread {
//            if (resourceId == R.string.connected) {
//                this@CooeyBodyAnalyzer.progressBar.visibility = View.GONE
//            }
//            this@CooeyBodyAnalyzer.connectionStatus.setText(resourceId)
//        }
//    }
//
//    private fun displayData(bluetoothGattCharacteristic: BluetoothGattCharacteristic) {
//        val charaProp = bluetoothGattCharacteristic.properties
//        if (charaProp or 2 > 0) {
//            if (this.mNotifyCharacteristic != null) {
//                this.mBluetoothLeService?.setCharacteristicNotification(this.mNotifyCharacteristic, false)
//                this.mNotifyCharacteristic = null
//            }
//            this.mBluetoothLeService?.readCharacteristic(bluetoothGattCharacteristic)
//        }
//        if (charaProp or 16 > 0) {
//            this.mNotifyCharacteristic = bluetoothGattCharacteristic
//            this.mBluetoothLeService?.setCharacteristicNotification(bluetoothGattCharacteristic, true)
//        }
//    }
//
//    private fun updateDataBatteryStatus(data: String?) {
//        if (data != null) {
//            progressBattery.progress = Integer.parseInt(data)
//            progressText.setText(getPercentage(data));
//        } else {
////            this.txtDevieBatteryStatus.setText("-")
//        }
//    }
//
//    private fun getPercentage(data: String?): String {
//        return data + " %"
//    }
//
//    private fun updateDataSystolicValues(stringExtra: String?) {
//        if (stringExtra != null) {
//
//            mSeries2.appendData(DataPoint(progress1 * 1.0, Integer.parseInt(stringExtra) * 1.0), false, 100)
//            progress1++;
//
////            this.txtProgressStatus.setText(stringExtra)
////            this.button.setText("Stop")
////            return
//        }
////        this.txtProgressStatus.setText("-")
//    }
//
//    private fun updateBPValues(systolicValue: Int, diastolicValue: Int, heartRateValue: Int) {
//
//
////        this.systolic = systolicValue
////        this.dystolic = diastolicValue
////        this.heartRate = heartRateValue
////        this.txtProgressStatus.setText(systolicValue.toString() + "/" + diastolicValue + "mmHg")
//        systolicText.setText("" + systolicValue)
//        distolicText.setText("" + diastolicValue)
//        hrText.setText("" + heartRateValue)
//        checkBPRange(systolicValue, diastolicValue)
//    }
//
//    private fun updateErrorDetails(stringExtra: String?) {
//        if (stringExtra != null) {
//
////            this.txtProgressStatus.setText("Error Occured" + stringExtra)
////            if (this.mConnected) {
////                this.button.setText(getString(R.string.start))
////            } else {
////                this.button.setText(getString(R.string.connect))
////            }
//        }
//    }
//
//    private fun checkBPRange(systolic: Int, diastolic: Int) {
//        var bpRangeTosend = 0
//        if (systolic < 90 && diastolic < 60) {
//            bpRangeTosend = 1
//        } else if (systolic < 90 && diastolic >= 60 && diastolic < 90) {
//            bpRangeTosend = 1
//        } else if (systolic < 90 && diastolic >= 90) {
//            bpRangeTosend = 4
//        } else if (systolic >= 90 && systolic < 140 && diastolic >= 90) {
//            bpRangeTosend = 3
//        } else if (systolic >= 90 && systolic < 140 && diastolic >= 60 && diastolic < 90) {
//            bpRangeTosend = 2
//        } else if (systolic >= 90 && systolic < 140 && diastolic < 60) {
//            bpRangeTosend = 1
//        } else if (systolic >= 140 && diastolic >= 90) {
//            bpRangeTosend = 3
//        } else if (systolic >= 140 && diastolic >= 60 && diastolic < 90) {
//            bpRangeTosend = 3
//        } else if (systolic >= 140 && diastolic < 60) {
//            bpRangeTosend = 5
//        }
//        setRangeOnUI(bpRangeTosend)
//    }
//
//    fun setRangeOnUI(bpRange: Int) {
//        if (bpRange > 0 && bpRange <= 5) {
////            if (this.txtBpRange.getVisibility() === View.GONE) {
////                this.txtBpRange.setVisibility(View.VISIBLE)
////            }
////            when (bpRange) {
////                1 -> {
////                    this.txtBpRange.setText("Your Blood Pressure is Low")
////                    return
////                }
////                2 -> {
////                    this.txtBpRange.setText("Your Blood Pressure is Normal")
////                    return
////                }
////                3 -> {
////                    this.txtBpRange.setText("Your Blood Pressure is High")
////                    return
////                }
////                4 -> {
////                    this.txtBpRange.setText("Your Blood Pressure is Low/High")
////                    return
////                }
////                5 -> {
////                    this.txtBpRange.setText("Your Blood Pressure is High/Low")
////                    return
////                }
////                else -> return
////            }
//        }
//    }
//
//}