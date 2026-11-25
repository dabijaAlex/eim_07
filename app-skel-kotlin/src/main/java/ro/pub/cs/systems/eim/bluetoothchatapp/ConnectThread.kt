package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class ConnectThread(
    private val mainActivity: MainActivity,
    private val bluetoothAdapter: BluetoothAdapter,
    device: BluetoothDevice,
    uuid: UUID
) : Thread() {

    private val socket: BluetoothSocket?

    init {
        var tmp: BluetoothSocket? = null
        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            try {
                tmp = device.createRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                Log.d("Connect->Constructor", e.toString())
            } catch (e: SecurityException) {
                Log.d("Connect->Constructor", "Missing permission: ${e.message}")
            }
        } else {
            Log.d("Connect->Constructor", "BLUETOOTH_CONNECT permission not granted.")
        }
        socket = tmp
    }



    override fun run() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (socket == null) {
            Log.d("ConnectThread", "Socket is null, cannot connect.")
            return
        }
        bluetoothAdapter.cancelDiscovery() // Oprește căutarea altor dispozitive
        try {
            socket.connect() // Se conectează la dispozitiv
            mainActivity.manageConnectedSocket(socket)
        } catch (connectException: IOException) {
            mainActivity.runOnUiThread { Toast.makeText(mainActivity, "Connection failed.", Toast.LENGTH_SHORT).show() }
            try {
                socket.close()
            } catch (closeException: IOException) {
                Log.d("Connect->Run", closeException.toString())
            }
        }
    }





    fun cancel() {
        try {
            if (socket == null) {
                return
            }
            socket.close()
        } catch (e: IOException) {
            Log.d("Connect->Cancel", e.toString())
        }
    }



    // BLUETOOTH_CONNECT / BLUETOOTH_SCAN only exist on Android 12+
    private fun hasPermission(permission: String): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ActivityCompat.checkSelfPermission(mainActivity, permission) ==
                PackageManager.PERMISSION_GRANTED
}