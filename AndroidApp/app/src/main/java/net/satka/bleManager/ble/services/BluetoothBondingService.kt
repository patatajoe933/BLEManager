package net.satka.bleManager.ble.services

import android.util.Log
import androidx.activity.ComponentActivity

class BluetoothBondingService(context: ComponentActivity) : BluetoothService(context) {
    companion object {
        private val CLASS_NAME = BluetoothBondingService::class.java.name
    }

    fun getBondState(macAddress: String) : Int? {
        bluetoothAdapter?.getRemoteDevice(macAddress)?.let { device ->
            try {
                return device.bondState
            }
            catch (e: SecurityException) {
                Log.e(CLASS_NAME, "exception", e)
            }
        }

        return null
    }
}