package com.example.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log

@SuppressLint("MissingPermission")
class BleManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {


    private var scan_callback: BleScanCallback = BleScanCallback()

    private var device_list: List<BluetoothDevice> = listOf()

    private var device: BluetoothDevice? = null

    private var gattCallback: BleGattCallback = BleGattCallback()

    private var socket: BluetoothSocket? = null

    private val scan_settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).setReportDelay(2000).build()

    fun scan(callback: (List<Pair<String, String>>) -> Unit) {
        bluetoothManager.adapter.bluetoothLeScanner.startScan(mutableListOf(),scan_settings,scan_callback { list ->
            bluetoothManager.adapter.bluetoothLeScanner.stopScan(scan_callback)
            device_list = list
            Log.i("BleManager","bubbeling scan callback up")
            callback(list.map { d -> Pair(d.name ?: "-no name given-", d.address) })
        })
    }

    fun select_device(mac: String): Boolean {
        device = device_list.find { d -> d.address == mac }
        return device != null
    }

    fun connect_gatt() {
        if (device == null) {
            Log.i("BleManager", "no device selectable")
            return
        }
        device!!.connectGatt(context, true, BleGattCallback())
    }

    fun disconnect_gatt() {
        if (device == null) {
            Log.i("BleManager", "no device selected")
            return
        }
        gattCallback.disconnect()
    }

    fun connect_l2cap_insecure(psm: Int) {
        if (device == null) {
            Log.i("BleManager", "no device selected")
            return
        }
        socket = device!!.createInsecureL2capChannel(psm)
    }

    fun connect_l2cap_secure(psm: Int) {
        if (device == null) {
            Log.i("BleManager", "no device selected")
            return
        }
        socket = device!!.createL2capChannel(psm)
    }

    fun disconnect_socket() {
        try {
            socket?.close()
            socket = null
        } catch (_: Exception) {
        }
    }

}

@SuppressLint("MissingPermission")
class BleGattCallback : BluetoothGattCallback() {

    private var gatt: BluetoothGatt? = null

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        this.gatt = gatt
    }

    fun disconnect() {
        try {
            gatt?.disconnect()
        } catch (_: Exception) {
        }
    }

}

class BleScanCallback : ScanCallback() {

    private var callback: (List<BluetoothDevice>) -> Unit = {}

    operator fun invoke(callback: (List<BluetoothDevice>) -> Unit): ScanCallback {
        this.callback = callback
        return this
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        Log.i("BleScanCallback","got ${results?.count()}")
        if (results != null) {
            callback(results.map { e -> e.device })
        }
    }
}

