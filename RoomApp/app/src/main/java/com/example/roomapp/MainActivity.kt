package com.example.roomapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import com.example.roomapp.model.Room
import com.example.roomapp.ui.theme.RoomAppTheme

@SuppressLint("InlinedApi", "MissingPermission")
class MainActivity : ComponentActivity() {
    private val scannedDevices = mutableStateOf<List<BluetoothDevice?>>(emptyList())
    private val pairedDevices: MutableList<BluetoothDevice> = ArrayList()
    private var btAdapter: BluetoothAdapter? = null


    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                scannedDevices.value = scannedDevices.value + device
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoomAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    var showDialog by remember { mutableStateOf(false) }
                    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
                    var rooms by remember { mutableStateOf(emptyList<Room>()) }
                    var nameroom by remember { mutableStateOf("") }
                    rooms = loadListData(context)
                    checkPermissionAndEnableBluetooth()

                    btAdapter = BluetoothAdapter.getDefaultAdapter()
                    pairedDevices.addAll(btAdapter?.bondedDevices!!)
                    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                    registerReceiver(bluetoothReceiver, filter)
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopAppBar(
                            title = { Text(text = "Rooms") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LazyColumn(verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top) {
                            items(rooms) { room ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .clickable {
                                            connectToDevice(room, context)
                                        }
                                        .fillMaxWidth()
                                        .padding(16.dp)

                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()){
                                        Icon(
                                            Icons.Outlined.Bed,
                                            "Localized description",
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(text = room.name)
                                            Text(text = room.device.name ?: "Unknown")
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            Icons.Outlined.Delete,
                                            "Localized description",
                                            modifier = Modifier.clickable {
                                                rooms = rooms.filter { it != room }
                                                saveListData(rooms, context)
                                            }.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            ExtendedFloatingActionButton(
                                modifier = Modifier.padding(16.dp),
                                onClick = { showDialog = true },
                                icon = { Icon(Icons.Filled.Add, "Localized description") },
                                text = { Text(text = "Add room") },
                            )
                        }
                        if (showDialog) {
                            Dialog(onDismissRequest = { showDialog = false }) {
                                ElevatedCard(modifier = Modifier.padding(16.dp)) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top,
                                        horizontalAlignment = Alignment.CenterHorizontally

                                    ) {
                                        Text(text = "Paired devices:")
                                        ElevatedCard(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .height(150.dp)
                                                .fillMaxWidth()
                                        )
                                        {

                                            LazyColumn(
                                                modifier = Modifier.weight(0.1f)
                                            ) {
                                                items(pairedDevices) { device ->
                                                    Text(text = device.name ?: "Unknown",
                                                        modifier = Modifier.clickable {
                                                            selectedDevice = device
                                                        })
                                                    HorizontalDivider()
                                                }
                                            }
                                        }
                                        Text(text = "New devices:")
                                        ElevatedCard(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .height(150.dp)
                                                .fillMaxWidth()
                                        )
                                        {

                                            LazyColumn(
                                                modifier = Modifier.weight(0.1f)
                                            ) {
                                                items(scannedDevices.value) { device ->
                                                    Text(text = device?.name ?: "Unknown",
                                                        modifier = Modifier.clickable {
                                                            selectedDevice = device
                                                        })
                                                    HorizontalDivider()
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (!btAdapter!!.isDiscovering) {
                                                    scannedDevices.value = emptyList()
                                                    btAdapter?.startDiscovery()
                                                }
                                            }, modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(text = "Scan new devices")
                                        }
                                        TextField(
                                            value = nameroom,
                                            onValueChange = { nameroom = it },
                                            label = { Text(text = "Room name") },
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth()
                                        )
                                        Button(
                                            onClick = {
                                                if (selectedDevice != null) {
                                                    rooms = rooms + Room(nameroom, selectedDevice!!)
                                                    showDialog = false
                                                } else {
                                                    rooms =
                                                        rooms + Room(nameroom, "00:00:00:00:00:00")
                                                    showDialog = false
                                                }
                                                saveListData(rooms, context)
                                            }, modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(text = "Add room")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveListData(list: List<Room>, context: Context) {
        val sharedPreferences =
            context.getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val set = list.map { "${it.name},${it.device.address}" }.toSet()
        editor.putStringSet("rooms", set)
        editor.apply()
    }

    private fun loadListData(context: Context): List<Room> {
        val sharedPreferences =
            context.getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
        val rooms = sharedPreferences.getStringSet("rooms", setOf())?.mapNotNull {
            val (name, address) = it.split(",")
            if (BluetoothAdapter.checkBluetoothAddress(address)) {
                Room(name, address)
            } else {
                null
            }
        }
        return rooms ?: emptyList()
    }


    private fun connectToDevice(room: Room, context: Context = this) {
        val intent = Intent(context, RoomActivity::class.java).apply {
            putExtra("device", room.device.address)
            putExtra("name", room.name)
        }
        startActivity(intent)
    }

    private fun checkPermissionAndEnableBluetooth() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    1
                )
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    ),
                    1
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(bluetoothReceiver)
    }
}
