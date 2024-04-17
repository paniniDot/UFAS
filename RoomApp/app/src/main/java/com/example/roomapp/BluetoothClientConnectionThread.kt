package com.example.roomapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.util.Log
import java.io.IOException
import java.util.UUID
import java.util.function.Consumer

@SuppressLint("MissingPermission")
class BluetoothClientConnectionThread(
    device: BluetoothDevice,
    private val btAdapter: BluetoothAdapter,
    var handler: Consumer<BluetoothSocket?>
) : Thread() {
    private val socket: BluetoothSocket?

    init {
        // Use a temporary object that is later assigned to socket
        // because socket is final.
        var tmp: BluetoothSocket? = null
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(DEFAULT_DEVICE_UUID)
        } catch (e: IOException) {
            Log.e(ContentValues.TAG, "Socket's create() method failed", e)
        }
        socket = tmp
    }

    override fun run() {
        // Cancel discovery because it otherwise slows down the connection.
        btAdapter.cancelDiscovery()
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            socket!!.connect()
        } catch (connectException: IOException) {
            Log.e(ContentValues.TAG, "unable to connect")
            // Unable to connect; close the socket and return.
            try {
                socket!!.close()
            } catch (closeException: IOException) {
                Log.e(ContentValues.TAG, "Could not close the client socket", closeException)
            }
            return
        }
        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        handler.accept(socket)
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            socket!!.close()
        } catch (e: IOException) {
            Log.e(ContentValues.TAG, "Could not close the client socket", e)
        }
    }

    companion object {
        private val DEFAULT_DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}