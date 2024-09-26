package com.example.ble

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context

class App: Application() {

    lateinit var bleManager: BleManager

    override fun onCreate() {
        super.onCreate()
        bleManager = BleManager(applicationContext,getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
    }
}