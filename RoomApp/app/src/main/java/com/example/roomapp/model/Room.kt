package com.example.roomapp.model

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

data class Room(
    val name: String,
    val device: BluetoothDevice
) {
    constructor(name: String, deviceID: String) : this(
        name,
        BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceID)
    )
}