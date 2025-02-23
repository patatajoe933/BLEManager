package net.satka.bleManager.ble.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK

abstract class BluetoothService(protected val context: ComponentActivity) {
    companion object {
        private const val GATT_CONN_TERMINATE_LOCAL_HOST: Int = 0x16
        fun getGattErrorMessage(status: Int): String {
            return when (status) {
                BluetoothGatt.GATT_SUCCESS -> "GATT Success"
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> "GATT Read Not Permitted"
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "GATT Write Not Permitted"
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> "GATT Insufficient Authentication"
                BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> "GATT Request Not Supported"
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> "GATT Insufficient Encryption"
                BluetoothGatt.GATT_INVALID_OFFSET -> "GATT Invalid Offset"
                BluetoothGatt.GATT_INSUFFICIENT_AUTHORIZATION -> "GATT Insufficient Authorization"
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "GATT Invalid Attribute Length"
                BluetoothGatt.GATT_CONNECTION_CONGESTED -> "GATT Connection Congested"
                BluetoothGatt.GATT_CONNECTION_TIMEOUT -> "GATT Connection Timeout"
                BluetoothGatt.GATT_FAILURE -> "GATT Failure"
                GATT_CONN_TERMINATE_LOCAL_HOST -> "GATT Connection Terminated due to Local Host"
                BluetoothDeviceConnectionService.NO_2902_DESCRIPTOR -> "Descriptor 2902 is Missing"
                BluetoothDeviceConnectionService.DESCRIPTOR_READ_FAILED -> "Descriptor Read Failed"
                BluetoothDeviceConnectionService.NO_PROPERTY_READ -> "Characteristic does not have Read Property"
                BluetoothDeviceConnectionService.NO_PROPERTY_WRITE -> "Characteristic does not have Write Property"
                BluetoothDeviceConnectionService.WRITE_ERROR -> "Write Error"
                BluetoothDeviceConnectionService.UNKNOWN_ERROR -> "Unknown Error"
                BluetoothDeviceConnectionService.NOT_CONNECTED -> "Not Connected"
                else -> "Unknown GATT Error (status: $status)"
            }
        }
    }

    private var isEnableBluetoothRejected = false
    private var enableBtLauncher: ActivityResultLauncher<Intent> = context.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != RESULT_OK && bluetoothAdapter?.isEnabled != true) {
            isEnableBluetoothRejected = true
        }
    }

    protected var bluetoothAdapter: BluetoothAdapter? = null
        private set

    protected fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    }

    protected fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false && !isEnableBluetoothRejected) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        }
    }

    private fun setBluetoothAdapter() {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    init {
        setBluetoothAdapter()
    }

}