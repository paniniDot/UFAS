package com.example.roomapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Blinds
import androidx.compose.material.icons.outlined.BlindsClosed
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Label
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.roomapp.ui.theme.RoomAppTheme
import java.io.OutputStream


class RoomActivity : ComponentActivity() {
    private var bluetoothOutputStream: OutputStream? = null
    private var connectionThread: BluetoothClientConnectionThread? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceID = intent.getStringExtra("device")
        val roomName = intent.getStringExtra("name")

        val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceID)
        val btAdapter = getSystemService(BluetoothManager::class.java).adapter
        connectionThread = BluetoothClientConnectionThread(
            device,
            btAdapter!!
        ) { socket: BluetoothSocket? ->
            bluetoothOutputStream = socket?.outputStream
        }
        connectionThread!!.start()

        setContent {
            RoomAppTheme {
                Surface {
                    var checked by remember { mutableStateOf(true) }
                    var sliderPosition by remember { mutableStateOf(0f) }
                    val interactionSource: MutableInteractionSource =
                        remember { MutableInteractionSource() }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TopAppBar(title = { Text(text = "Room: $roomName") })
                        ElevatedCard(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                val icon: (@Composable () -> Unit) = if (checked) {
                                    {
                                        Icon(
                                            Icons.Outlined.Lightbulb,
                                            "Localized description"
                                        )
                                    }
                                } else {
                                    {
                                        Icon(
                                            Icons.Outlined.Lightbulb,
                                            "Localized description",
                                        )
                                    }
                                }
                                Text(text = "Lampadario")
                                Spacer(modifier = Modifier.weight(1f))
                                Switch(
                                    checked = checked,
                                    onCheckedChange = { checked = it },
                                    thumbContent = icon
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        ElevatedCard(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(text = "Tapparella: $sliderPosition")
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Outlined.Blinds,
                                        "Localized description",
                                    )
                                    Slider(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 16.dp)
                                            .semantics { contentDescription = "Slider" },
                                        value = sliderPosition,
                                        onValueChange = { sliderPosition = it },
                                        valueRange = 0f..100f,
                                        interactionSource = interactionSource,
                                        onValueChangeFinished = {
                                            // launch some business logic update with the state you hold
                                            // viewModel.updateSelectedSliderValue(sliderPosition)
                                        },
                                        thumb = {
                                            Label(
                                                label = {
                                                    PlainTooltip(
                                                        modifier = Modifier
                                                            .requiredSize(45.dp, 25.dp)
                                                            .wrapContentWidth()
                                                    ) {
                                                        Text("%.2f".format(sliderPosition))
                                                    }
                                                },
                                                interactionSource = interactionSource
                                            ) {
                                                Icon(
                                                    Icons.Filled.RadioButtonChecked,
                                                    "Localized description",
                                                )
                                            }
                                        }
                                    )
                                    Icon(
                                        Icons.Outlined.BlindsClosed,
                                        "Localized description",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
