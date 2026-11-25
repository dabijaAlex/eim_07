package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

// BluetoothSocket este canalul de comunicatie stabilit cu un alt device bluetooth.
class ConnectedThread(
    private val mainActivity: MainActivity,
    private val socket: BluetoothSocket
) : Thread() {

    private val inputStream: InputStream?
    private val outputStream: OutputStream?

    init {
        var tmpIn: InputStream? = null
        var tmpOut: OutputStream? = null

        try {
            tmpIn = socket.inputStream
        } catch (e: IOException) {
            Log.d("Connected->Constructor", "Failed to get input stream: ${e.message}")
        }

        try {
            tmpOut = socket.outputStream
        } catch (e: IOException) {
            Log.d("Connected->Constructor", "Failed to get output stream: ${e.message}")
        }

        inputStream = tmpIn
        outputStream = tmpOut
    }



    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // obtine numele device-ului cu care comunicam
        val remoteDeviceName = socket.getRemoteDevice().name

        mainActivity.runOnUiThread { Toast.makeText(mainActivity, "Connected to $remoteDeviceName", Toast.LENGTH_SHORT).show() }

        while (true) {
            try {
                bytes = inputStream!!.read(buffer)  // Citește mesajul
                val incomingMessage = String(buffer, 0, bytes)

                // Adaugam mesajul in threadul de UI al aplicatiei
                mainActivity.runOnUiThread { mainActivity.addChatMessage("$remoteDeviceName: $incomingMessage") }
            } catch (e: IOException) {
                mainActivity.runOnUiThread { Toast.makeText(mainActivity, "Connection lost.", Toast.LENGTH_SHORT).show() }
                break
            }
        }
    }





    fun write(bytes: ByteArray) {
        try {
            outputStream!!.write(bytes)
        } catch (e: IOException) {
            mainActivity.runOnUiThread { Toast.makeText(mainActivity, "Failed to send message.", Toast.LENGTH_SHORT).show() }
            Log.d("Connected->Write", e.toString())
        }
    }





    fun cancel() {
        try {
            if (socket == null) {
                Log.d("ConnectedThread", "Socket is null, cannot close.")
                return
            }
            socket.close()
        } catch (e: IOException) {
            Log.d("Connected->Cancel", e.toString())
        }
    }


}