package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class AcceptThread(
    private val mainActivity: MainActivity,
    adapter: BluetoothAdapter,
    uuid: UUID
) : Thread() {

    private val serverSocket: BluetoothServerSocket?

    init {
        var tempSocket: BluetoothServerSocket? = null
        val hasPermission =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    ActivityCompat.checkSelfPermission(
                        mainActivity, Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.d("AcceptThread", "BLUETOOTH_CONNECT permission not granted.")
        } else {
            try {
                tempSocket = adapter.listenUsingRfcommWithServiceRecord("BluetoothChatApp", uuid)
            } catch (e: IOException) {
                Log.e("AcceptThread", "Error creating server socket: ${e.message}")
            } catch (e: SecurityException) {
                Log.e("AcceptThread", "Missing Bluetooth permission: ${e.message}")
            }
        }
        serverSocket = tempSocket
    }


    private val TAG = "my-tag"

    override fun run() {
        if (serverSocket == null) {
            Log.d(TAG, "ServerSocket is null, cannot accept connections.")
            return
        }

        var socket: BluetoothSocket
        while (!Thread.currentThread().isInterrupted) {
            try {
                // Așteaptă o conexiune
                socket = serverSocket.accept()
                if (socket != null) {
                    Log.d(TAG, "Connection accepted. Managing socket...")

                    // Predă conexiunea aplicației
                    mainActivity.manageConnectedSocket(socket)
                    closeServerSocket()
                    break
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error while accepting connection: ${e.message}")
                break
            }
        }
        Log.d(TAG, "AcceptThread exiting.")
    }





    fun cancel() {
        Log.d(TAG, "Canceling AcceptThread...")
        closeServerSocket()
    }

    private fun closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close()
                Log.d(TAG, "ServerSocket closed successfully.")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error closing ServerSocket: ${e.message}")
        }
    }


}