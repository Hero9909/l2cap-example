package com.example.ble

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.ble.ui.theme.MyApplicationTheme
import java.util.Stack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        var devices: SnapshotStateList<Pair<String, String>> = remember { mutableStateListOf() }
                        val selected_device: MutableState<String?> =
                            remember { mutableStateOf(null) }
                        val device_name = remember { mutableStateOf("") }
                        Row {
                            Button({
                                selected_device.value = null
                            }) {
                                Text("Clear")
                            }
                            Column {
                                Text(device_name.value)
                                Text(selected_device.value ?: "", fontSize = 10.sp)
                            }
                        }
                        if (selected_device.value == null) {
                            ScanAndSelect(application,devices) { d ->
                                if (application.BleManager().select_device(d.second)) {
                                    selected_device.value = d.second
                                    device_name.value = d.first
                                }
                            }
                        } else {
                            BleActions(application)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ScanAndSelect(
    application: Application,
    devices: SnapshotStateList<Pair<String,String>>,
    on_select_device: (Pair<String, String>) -> Unit
) {
    val scanning = remember { mutableStateOf(false) }
    Row {
        IconButton({
            scanning.value = true
            application.BleManager().scan { l ->
                Log.i("compose", "got callback")
                devices.clear()
                devices.addAll(l)
                scanning.value = false
            }
        }) {
            Icon(imageVector = Icons.Filled.Search, "")
        }
        if (scanning.value){
            CircularProgressIndicator()
        }
    }
    for (device in devices) {
        Column(Modifier.clickable {
            on_select_device(device)
        }) {
            Text(device.first)
            Text(device.second)
        }
    }
}

@Composable
fun ColumnScope.BleActions(
    application: Application
) {
    Column(Modifier.align(Alignment.CenterHorizontally)) {
        Button({
            application.BleManager().connect_gatt()
        }) {
            Text("connect gatt")
        }
        Button({
            application.BleManager().disconnect_gatt()
        }) {
            Text("disconnect gatt")
        }
        val id = remember { mutableStateOf("") }
        TextField(id.value, { new -> id.value = new })
        Button({
            application.BleManager().connect_l2cap_secure(id.value.toInt())
        }, enabled = id.value.toIntOrNull() != null) {
            Text("connect l2cap secure")
        }
        Button({
            application.BleManager().connect_l2cap_insecure(id.value.toInt())
        }, enabled = id.value.toIntOrNull() != null) {
            Text("connect l2cap insecure")
        }

        Button({
            application.BleManager().disconnect_socket()
        }) {
            Text("disconnect socket")
        }
    }
}

fun Application.BleManager(): BleManager {
    return (this as App).bleManager
}
