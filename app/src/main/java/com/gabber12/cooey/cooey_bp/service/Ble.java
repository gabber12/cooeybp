package com.gabber12.cooey.cooey_bp.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Ble {
    private static String TAG = "Runtime_ble";
    private String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private int connectTimeoutInterval = 20000;
    private Timer connectTimer = new Timer();
    private TimerTask connectTimerTask = null;
    private List<String> connectedMacList = new ArrayList();
    private List<String> connectingMacList = new ArrayList();
    private boolean isSettingIndication = false;
    private boolean isSettingNotification = false;
    private BleCallback mBleCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback = new C17512();
    private BluetoothGattCharacteristic mGattCharacteristicTrans;
    private BluetoothGattService mGattServiceComm;
    private BluetoothGattService mGattServiceIDPS;
    private BluetoothGattCharacteristic mGattcharacteristicReceive;
    public LeScanCallback mLeScanCallback = new C17501();
    private Map<String, BluetoothGatt> mapBleGatt = new ConcurrentHashMap();
    private Map<String, BluetoothGattCharacteristic> mapBleRec = new ConcurrentHashMap();
    private Map<String, BluetoothGattCharacteristic> mapBleTrans = new ConcurrentHashMap();
    private List<String> reConnectMacList = new ArrayList();
    private BluetoothGatt refreshGatt;
    private TimerTask refreshTask;
    private Timer refreshTimer;

    class C17501 implements LeScanCallback {
        C17501() {
        }

        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            Ble.this.mBleCallback.onScanResult(bluetoothDevice, i, bArr);
        }
    }

    class C17512 extends BluetoothGattCallback {
        C17512() {
        }

        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int i, int i2) {
            Ble.this.mBleCallback.onRssi(i);
        }

        public void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
        }

        public synchronized void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.e(Ble.TAG, "ble connection mac:" + bluetoothGatt.getDevice().getAddress() + " --- status:" + i + " --- newState:" + i2);
            String address = bluetoothGatt.getDevice().getAddress();
            String replace = address.replace(":", "");
            if (i != 0) {
                Log.i(Ble.TAG, "BluetoothGatt Status Error:" + address);
                Ble.this.commandCheckReconnectDevice(bluetoothGatt, i, i2);
            } else if (i2 == 2) {
                Ble.this.mapBleGatt.put(replace, Ble.this.mBluetoothGatt);
                Ble.this.commandCancelTimeoutForDevice(address);
                if (Ble.this.reConnectMacList.contains(address)) {
                    Ble.this.reConnectMacList.remove(address);
                }
                if (Ble.this.connectingMacList.contains(address)) {
                    Ble.this.connectingMacList.remove(address);
                }
                if (Ble.this.connectedMacList.contains(address)) {
                    Log.i(Ble.TAG, "Duplicate connection success");
                } else {
                    Ble.this.connectedMacList.add(address);
                    Log.i(Ble.TAG, "Connection Success");
                    Ble.this.mBleCallback.onConnectionStateChange(bluetoothGatt.getDevice(), i, i2);
                    SystemClock.sleep(300);
                    bluetoothGatt.discoverServices();
                }
            } else if (i2 == 0) {
                Ble.this.commandCheckReconnectDevice(bluetoothGatt, i, i2);
            } else {
                Log.i(Ble.TAG, "Connection Status Change:" + i2);
                Ble.this.mBleCallback.onConnectionStateChange(bluetoothGatt.getDevice(), i, i2);
            }
        }

        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            if (i == 0) {
                Log.d(Ble.TAG, "create ble services success");
                List<BluetoothGattService> services = bluetoothGatt.getServices();
                List arrayList = new ArrayList();
                for (BluetoothGattService bluetoothGattService : services) {
                    arrayList.add(bluetoothGattService.getUuid());
                    for (BluetoothGattCharacteristic uuid : bluetoothGattService.getCharacteristics()) {
                        arrayList.add(uuid.getUuid());
                    }
                }
                Ble.this.mBleCallback.onServicesDiscovered(bluetoothGatt.getDevice(), arrayList, i);
                return;
            }
            Log.e(Ble.TAG, "create ble services fail");
            bluetoothGatt.disconnect();
        }

        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            if (i == 0) {
                UUID uuid = bluetoothGattDescriptor.getCharacteristic().getUuid();
                if (Ble.this.isSettingNotification) {
                    Ble.this.mBleCallback.onServicesObtain();
                    Ble.this.mBleCallback.onServicesObtain(uuid, bluetoothGatt.getDevice(), null);
                    Ble.this.isSettingNotification = false;
                    return;
                } else if (Ble.this.isSettingIndication) {
                    Ble.this.mBleCallback.onServicesObtain();
                    Ble.this.mBleCallback.onServicesObtain(uuid, bluetoothGatt.getDevice(), null);
                    Ble.this.isSettingIndication = false;
                    return;
                } else {
                    return;
                }
            }
            Log.e(Ble.TAG, "Descriptor Write fail");
            bluetoothGatt.disconnect();
        }

        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0) {
                Ble.this.mBleCallback.onCharacteristicRead(bluetoothGatt.getDevice(), bluetoothGattCharacteristic.getValue(), bluetoothGattCharacteristic.getUuid(), i);
                return;
            }
            Log.e(Ble.TAG, "Characteristic Read fail");
            bluetoothGatt.disconnect();
        }

        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            Ble.this.mBleCallback.onCharacteristicChanged(bluetoothGatt.getDevice(), bluetoothGattCharacteristic.getValue(), bluetoothGattCharacteristic.getUuid());
        }

        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0) {
                Ble.this.mBleCallback.onCharacteristicWrite(bluetoothGatt.getDevice(), bluetoothGattCharacteristic.getUuid(), i);
            } else {
                Log.e(Ble.TAG, "Characteristic Write fail");
            }
        }
    }

    class C17534 extends TimerTask {
        C17534() {
        }

        public void run() {
            Ble.this.refreshGatt.disconnect();
        }
    }

    class C17545 extends TimerTask {
        C17545() {
        }

        public void run() {
            Ble.this.refreshGatt.disconnect();
        }
    }

    public Ble(Context context, BleCallback bleCallback) {
        this.mContext = context;
        this.mBleCallback = bleCallback;
        this.mBluetoothAdapter = ((BluetoothManager) this.mContext.getSystemService("bluetooth")).getAdapter();
        this.mapBleGatt.clear();
        this.mapBleTrans.clear();
        this.mapBleTrans.clear();
        this.reConnectMacList.clear();
        this.connectingMacList.clear();
        this.connectedMacList.clear();
    }

    private void close(String str) {
        SystemClock.sleep(500);
        if (this.mapBleGatt.get(str) != null) {
            ((BluetoothGatt) this.mapBleGatt.get(str)).disconnect();
            ((BluetoothGatt) this.mapBleGatt.get(str)).close();
            this.mapBleGatt.remove(str);
            this.mapBleTrans.remove(str);
            this.mapBleRec.remove(str);
            return;
        }
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.disconnect();
            this.mBluetoothGatt.close();
        }
        this.mBluetoothGatt = null;
    }

    public void scan(boolean z) {
        if (z) {
            Log.i(TAG, "scan:" + z);
            this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            return;
        }
        Log.i(TAG, "scan:" + z);
        this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
    }

    public boolean connectDevice(String str) {
        if (this.mBluetoothAdapter == null || str == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        } else if (this.connectingMacList.size() > 0 || this.connectingMacList.contains(str) || this.connectedMacList.contains(str)) {
            return false;
        } else {
            this.connectingMacList.add(str);
            BluetoothDevice remoteDevice = this.mBluetoothAdapter.getRemoteDevice(str);
            if (remoteDevice == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }
            this.mBluetoothGatt = remoteDevice.connectGatt(this.mContext, false, this.mGattCallback);
            Log.d(TAG, "Trying to create a new connection.");
            if (this.mBluetoothGatt == null) {
                return false;
            }
            commandAddTimeoutForDevice(str, this.mBluetoothGatt, 0, 0);
            return true;
        }
    }

    private void commandAddTimeoutForDevice(final String str, final BluetoothGatt bluetoothGatt, int i, int i2) {
        if (this.connectTimerTask != null) {
            this.connectTimerTask.cancel();
            this.connectTimerTask = null;
        }
        this.connectTimerTask = new TimerTask() {
            public void run() {
                Log.e(Ble.TAG, "Connection timeout");
                Ble.this.disconnect(str);
                SystemClock.sleep(300);
                Ble.this.commandCheckReconnectDevice(bluetoothGatt, 0, 0);
            }
        };
        this.connectTimer.schedule(this.connectTimerTask, (long) this.connectTimeoutInterval);
    }

    private void commandCancelTimeoutForDevice(String str) {
        if (this.connectTimerTask != null) {
            this.connectTimerTask.cancel();
            this.connectTimerTask = null;
        }
    }

    private void commandCheckReconnectDevice(BluetoothGatt bluetoothGatt, int i, int i2) {
        String address = bluetoothGatt.getDevice().getAddress();
        String replace = address.replace(":", "");
        commandCancelTimeoutForDevice(address);
        if (this.connectedMacList.contains(address)) {
            this.connectedMacList.remove(address);
        }
        Object obj = null;
        if (this.connectingMacList.contains(address)) {
            Log.i(TAG, "Connect failed");
            if (!this.reConnectMacList.contains(address) && bluetoothGatt.connect()) {
                Log.e(TAG, "Reconnect again");
                this.reConnectMacList.add(address);
                obj = 1;
                commandAddTimeoutForDevice(address, bluetoothGatt, i, i2);
            }
        } else {
            Log.i(TAG, "DisConnection Success");
        }
        if (obj == null) {
            close(replace);
            this.reConnectMacList.remove(address);
            if (this.connectingMacList.contains(address)) {
                this.connectingMacList.remove(address);
            }
            this.mBleCallback.onConnectionStateChange(bluetoothGatt.getDevice(), i, i2);
            return;
        }
        this.mBleCallback.onConnectionStateChange(bluetoothGatt.getDevice(), i, 4);
    }

    public boolean readRemoteRssi(String str) {
        if (this.mapBleGatt.get(str) != null) {
            return ((BluetoothGatt) this.mapBleGatt.get(str)).readRemoteRssi();
        }
        Log.i(TAG, "readRemoteRssi == null");
        return false;
    }

    public void disconnect(String str) {
        if (str.length() == 12) {
            String str2 = "";
            for (int i = 0; i < 6; i++) {
                str2 = new StringBuilder(String.valueOf(str2)).append(str.substring(i * 2, (i * 2) + 2)).toString();
                if (i < 5) {
                    str2 = new StringBuilder(String.valueOf(str2)).append(":").toString();
                }
            }
            Log.i(TAG, "want to disconnect:" + str2);
            if (this.connectingMacList.contains(str2)) {
                this.connectingMacList.remove(str2);
                commandCancelTimeoutForDevice(str);
            }
            BluetoothGatt bluetoothGatt = (BluetoothGatt) this.mapBleGatt.get(str);
            BluetoothGattCharacteristic bluetoothGattCharacteristic = (BluetoothGattCharacteristic) this.mapBleRec.get(str);
            if (bluetoothGatt != null) {
                disableNOtifications(bluetoothGatt, bluetoothGattCharacteristic);
                SystemClock.sleep(300);
                bluetoothGatt.disconnect();
            } else if (this.mBluetoothGatt != null) {
                disableNOtifications(bluetoothGatt, this.mGattcharacteristicReceive);
                SystemClock.sleep(300);
                this.mBluetoothGatt.disconnect();
            }
        }
    }

    public void getCommService(String str, UUID uuid, UUID uuid2, UUID uuid3, UUID uuid4, boolean z) {
        BluetoothGatt bluetoothGatt = (BluetoothGatt) this.mapBleGatt.get(str);
        this.mGattServiceIDPS = bluetoothGatt.getService(uuid4);
        if (this.mGattServiceIDPS != null) {
            Log.i(TAG, "mGattServiceIDPS --- OK");
            this.mGattServiceComm = bluetoothGatt.getService(uuid);
            if (this.mGattServiceComm != null) {
                Log.i(TAG, "mGattServiceComm --- OK");
                if (uuid2 != null) {
                    this.mGattCharacteristicTrans = this.mGattServiceComm.getCharacteristic(uuid2);
                }
                if (this.mGattCharacteristicTrans != null) {
                    Log.i(TAG, "mGattCharacteristicTrans --- OK");
                } else {
                    for (BluetoothGattService characteristics : bluetoothGatt.getServices()) {
                        for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics.getCharacteristics()) {
                            if (bluetoothGattCharacteristic.getUuid().equals(uuid2)) {
                                this.mGattCharacteristicTrans = bluetoothGattCharacteristic;
                                Log.i(TAG, "mGattCharacteristicTrans ---- OK");
                                break;
                            }
                        }
                    }
                    Log.i(TAG, "mGattCharacteristicTrans --- Null");
                }
                if (this.mGattCharacteristicTrans != null) {
                    this.mGattCharacteristicTrans.setWriteType(1);
                    this.mapBleTrans.put(str, this.mGattCharacteristicTrans);
                }
                this.mGattcharacteristicReceive = this.mGattServiceComm.getCharacteristic(uuid3);
                if (this.mGattcharacteristicReceive != null) {
                    Log.i(TAG, "mGattcharacteristicReceive --- OK");
                } else {
                    Log.e(TAG, "mGattcharacteristicReceive --- ERROR");
                }
                SystemClock.sleep(300);
                if (z) {
                    enableIndications(this.mGattcharacteristicReceive, bluetoothGatt);
                    return;
                } else if (enableNotifications(this.mGattcharacteristicReceive, bluetoothGatt)) {
                    this.mapBleRec.put(str, this.mGattcharacteristicReceive);
                    return;
                } else {
                    Log.i(TAG, "enableNotifications(true, mGattcharacteristicReceive) ---> false");
                    bluetoothGatt.disconnect();
                    return;
                }
            }
            Log.e(TAG, "mGattServiceComm --- ERROR");
            bluetoothGatt.disconnect();
            return;
        }
        Log.e(TAG, "mGattServiceIDPS --- ERROR");
        bluetoothGatt.disconnect();
    }

    public boolean readCharacteristicExtra(String str, UUID uuid, UUID uuid2) {
        BluetoothGatt bluetoothGatt = (BluetoothGatt) this.mapBleGatt.get(str);
        if (bluetoothGatt == null || uuid == null || uuid2 == null) {
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(uuid);
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid2);
        if (characteristic == null) {
            return false;
        }
        return bluetoothGatt.readCharacteristic(characteristic);
    }

    public void readCharacteristic(UUID uuid) {
        if (this.mGattServiceIDPS == null) {
            Log.e(TAG, "Device Information Service Not Found!!!");
            return;
        }
        BluetoothGattCharacteristic characteristic = this.mGattServiceIDPS.getCharacteristic(uuid);
        if (characteristic == null) {
            refreshDeviceCache(this.mBluetoothGatt);
            this.refreshGatt = this.mBluetoothGatt;
            cancelRefresh();
            this.refreshTimer = new Timer();
            this.refreshTask = new C17534();
            this.refreshTimer.schedule(this.refreshTask, 4000);
            return;
        }
        this.mBluetoothGatt.readCharacteristic(characteristic);
    }

    private final boolean enableNotifications(BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGatt bluetoothGatt) {
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null || !bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true)) {
            return false;
        }
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString(this.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor == null) {
            return false;
        }
        Log.i(TAG, "enable notification");
        this.isSettingNotification = true;
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        return this.mBluetoothGatt.writeDescriptor(descriptor);
    }

    private boolean disableNOtifications(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null || !bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, false)) {
            return false;
        }
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString(this.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor == null) {
            return false;
        }
        Log.i(TAG, "disable notification");
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        return bluetoothGatt.writeDescriptor(descriptor);
    }

    private final boolean enableIndications(BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGatt bluetoothGatt) {
        this.isSettingIndication = true;
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null) {
            Log.i(TAG, "gatt == null || characteristic == null");
            return false;
        } else if (!bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true)) {
            return false;
        } else {
            Log.i(TAG, "enable indications");
            BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString(this.CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor == null) {
                return false;
            }
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            return bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public void sendData(String str, byte[] bArr) {
        BluetoothGatt bluetoothGatt = (BluetoothGatt) this.mapBleGatt.get(str);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = (BluetoothGattCharacteristic) this.mapBleTrans.get(str);
        if (bluetoothGatt == null) {
            Log.e(TAG, "device is null");
        } else if (bluetoothGattCharacteristic == null) {
            Log.e(TAG, "CHRASTERISTIC_SEND is not created");
        } else {
            if (bluetoothGattCharacteristic.getUuid().toString().equals("0000ff03-0000-1000-8000-00805f9b34fb")) {
                Log.i(TAG, "WRITE_TYPE_SIGNED");
                bluetoothGattCharacteristic.setWriteType(2);
            }
            bluetoothGattCharacteristic.setValue(bArr);
            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        }
    }

    public boolean refresh(String str) {
        BluetoothGatt bluetoothGatt = (BluetoothGatt) this.mapBleGatt.get(str);
        if (bluetoothGatt == null) {
            return false;
        }
        this.refreshGatt = bluetoothGatt;
        cancelRefresh();
        this.refreshTimer = new Timer();
        this.refreshTask = new C17545();
        this.refreshTimer.schedule(this.refreshTask, 4000);
        return refreshDeviceCache((BluetoothGatt) this.mapBleGatt.get(str));
    }

    private boolean refreshDeviceCache(BluetoothGatt bluetoothGatt) {
        try {
            Method method = bluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (method != null) {
                return ((Boolean) method.invoke(bluetoothGatt, new Object[0])).booleanValue();
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

    private void cancelRefresh() {
        if (this.refreshTask != null) {
            this.refreshTask.cancel();
            this.refreshTask = null;
        }
        if (this.refreshTimer != null) {
            this.refreshTimer.cancel();
            this.refreshTimer = null;
        }
    }

    public void commandClearCache() {
        this.reConnectMacList.clear();
        this.connectingMacList.clear();
        this.connectedMacList.clear();
    }
}
