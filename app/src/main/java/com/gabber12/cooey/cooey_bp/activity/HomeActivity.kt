package com.gabber12.cooey.cooey_bp.activity

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.gabber12.cooey.cooey_bp.R

import kotlinx.android.synthetic.main.activity_home.*
import com.gabber12.cooey.cooey_bp.service.BluetoothLeService
import android.annotation.SuppressLint
import android.app.Activity
import android.support.v4.content.ContextCompat
import android.os.Build.VERSION
import android.bluetooth.*
import android.content.*
import com.gabber12.cooey.cooey_bp.service.GattAttributes
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.content_home.*


class HomeActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val SCAN_PERIOD: Long = 10000
    private val TAG = HomeActivity::class.java!!.getSimpleName()
    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"
    private val deviceList = ArrayList<Device>()
    var btnContinue: Button? = null
    var button: Button? = null
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val deviceHash = HashMap<String, Boolean>()
    private var adapter: ArrayAdapter<String>? = null
    var dystolic: Int = 0
    var frameAnimation: AnimationDrawable? = null
    var heartRate: Int = 0
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothLeService: BluetoothLeService? = null
    private var mConnected = false
    private var mDeviceAddress: String? = null
    private var mGattUpdateReceiver: BroadcastReceiver? = null
    private val mHandler: Handler = Handler()
    private val mLeScanCallback = (object : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            Log.i("Home", "Discovered" )
            this@HomeActivity.runOnUiThread(object : Runnable {
                @SuppressLint("MissingPermission")
                override fun run() {

                    if (null != device) {


                        var deviceName = device.getName();
                        val address:String = device.address
                        var deviceLabel = device.getName();
                        device.type

                        Log.i("Home", "Discovered " + deviceName + " "+ address)
                        //device.type
                        if (address != null && address.length > 0) {
                            Log.i("Home", address+ deviceName);
                            if(deviceHash.containsKey(address)) {
                                return;
                            }
                            if(deviceName == "01257B2C2BCB33"){
                                deviceLabel = deviceName
                                deviceName = "Cooey Body Analyzer(Mock)"
                            }
                            if(deviceName == "Technaxx BP"){
                                deviceLabel = deviceName
                                deviceName = "Cooey BP Meter"
                            }
                            if(deviceName == "11257B"){
                                deviceLabel = deviceName
                                deviceName = "Cooey Body Analyzer"
                            }
                            if(deviceName == ""){
                                deviceLabel = deviceName
                                deviceName = "Unknown"
                            }
                            deviceList.add(Device(address, deviceName, deviceLabel));
                            adapter?.notifyDataSetChanged()
                            deviceHash.put(address, true);
//                            if (deviceName.equals("Technaxx BP", true) || deviceName.equals("BPM-188", true)) {
//
//
//
//                            }
                        }
                    }
                }
            });
        }
    })
    private val mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var mScanning: Boolean = false
    private val mServiceConnection = (object: ServiceConnection {
        
        
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            this@HomeActivity.mBluetoothLeService = (service as BluetoothLeService.LocalBinder)?.getService();
            if (!this@HomeActivity.mBluetoothLeService!!.initialize()) {

                this@HomeActivity.finish();
            }
            this@HomeActivity.mBluetoothLeService!!.connect(this@HomeActivity.mDeviceAddress);
        }

        
        override fun onServiceDisconnected( componentName:ComponentName) {
            this@HomeActivity.mBluetoothLeService = null;
        }
    })

    private val progressBar: ImageView? = null
    private val progressBarAnimation: ImageView? = null
    var systolic: Int = 0
    var txtBpRange: TextView? = null
    var txtConnectionStatus: TextView? = null
    var txtDeviceName: TextView? = null
    var txtDevieBatteryStatus: TextView? = null
    var txtProgressStatus: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)


        scanButton.setOnClickListener {
            scanButton.setText(R.string.scanning);
            deviceList.clear()
            deviceHash.clear()
            scanLeDevice(true);
            adapter?.notifyDataSetChanged()
        }
        if (!(isBluetoothEnabled() || isLocationEnabled())) {
            getPermissions()
        }
        if (!packageManager.hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }
        this.mBluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (this.mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        adapter = CustomList(this, deviceList)
        device_list.adapter = adapter
        device_list.onItemClickListener = (object: AdapterView.OnItemClickListener{
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                // get device type
                var device: Device = deviceList[position]

//               if(device.deviceName == "Technaxx BP") {
//                    activity =  TechnaxxBP::class.java
//                } else {
//                var activity =  CooeyBodyAnalyzer::class.java
                var activity =  TechnaxxBP::class.java
//                }

                val intent = Intent(applicationContext, activity) //rep
                intent.putExtra("address", deviceList.get(position).address)
                startActivity(intent)
            }
        });
        bindService(Intent(this, BluetoothLeService::class.java), this.mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    @SuppressLint("MissingPermission")
    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            this.mHandler?.postDelayed((object: Runnable{
                @SuppressLint("MissingPermission")
                override fun run() {
                    Log.i("HOME", "Stopping")
                    mBluetoothAdapter?.stopLeScan(this@HomeActivity.mLeScanCallback)
                    this@HomeActivity.runOnUiThread((object: Runnable{
                        override fun run() {
                            mScanning=false
                            scanButton.setText(R.string.scan)
                        }
                    }))

                }
            }), SCAN_PERIOD)
            this.mScanning = true
            this.mBluetoothAdapter?.startLeScan(this.mLeScanCallback)
            return
        }
        this.mScanning = false
        this.mBluetoothAdapter?.stopLeScan(this.mLeScanCallback)
    }

    override fun onPause() {
        super.onPause()
        if (this.mConnected) {
            scanLeDevice(false)
        }
        if (this.mGattUpdateReceiver != null) {
            try {
                unregisterReceiver(this.mGattUpdateReceiver)
                this.mGattUpdateReceiver = null
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this.mServiceConnection)
        this.mBluetoothLeService = null
    }

    private fun updateConnectionState(resourceId: Int) {
        runOnUiThread {
            if (resourceId == R.string.disconnected) {
                this@HomeActivity.button?.setText(this@HomeActivity.getString(R.string.connect))
            }
            this@HomeActivity.txtConnectionStatus?.setText(resourceId)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (!this.mBluetoothAdapter?.isEnabled()!! && (this.mBluetoothAdapter == null || !this.mBluetoothAdapter!!.isEnabled())) {
            startActivityForResult(Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1)
        }
        registerReceiver(this.mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (this.mBluetoothLeService != null) {
        }
    }

    @SuppressLint("MissingPermission")
    fun isBluetoothEnabled(): Boolean {
        try {
            return BluetoothAdapter.getDefaultAdapter().enable()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }

    }

    fun isLocationEnabled(): Boolean {
        return ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0
    }

    protected fun getPermissions() {

        if (VERSION.SDK_INT < 23) {
            turnOnBluetooth(true)
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            turnOnBluetooth(true)
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("This app needs location access")
            builder.setMessage("Please grant location access so this app can detect beacons.")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener(DialogInterface.OnDismissListener { requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION) })
            builder.show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun turnOnBluetooth(turnOnOrOff: Boolean) {
        if (turnOnOrOff) {
            try {
                BluetoothAdapter.getDefaultAdapter().enable()
                return
            } catch (ex: Exception) {
                ex.printStackTrace()
                return
            }

        }
        try {
            BluetoothAdapter.getDefaultAdapter().disable()
        } catch (ex2: Exception) {
            ex2.printStackTrace()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1224 -> if (grantResults.size <= 0 || grantResults[0] != 0) {
                Toast.makeText(this, "Cannot scan devices without permissions.", Toast.LENGTH_SHORT).show()
                return
            } else {
                turnOnBluetooth(true)
                return
            }
            else -> return
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        return intentFilter
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices != null) {
            for (gattService in gattServices) {
                if (gattService.uuid.toString() == GattAttributes.SERVICE_UUID) {
                    for (gattCharacteristic in gattService.characteristics) {
                        if (gattCharacteristic.uuid.toString() == GattAttributes.SERVICE_READ_CHANNEL) {
                        }
                    }
                }
            }
        }
    }

}
