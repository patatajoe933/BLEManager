package net.satka.bleManager.ble.services

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import net.satka.bleManager.R
import net.satka.bleManager.constants.PermissionRequests.REQUEST_BLUETOOTH_PERMISSION_FOR_DISCOVERY

class BluetoothDiscoveryService(context: ComponentActivity) : BluetoothService(context) {
    companion object {
        private val CLASS_NAME = BluetoothDiscoveryService::class.java.name
    }

    private lateinit var receiver: BroadcastReceiver

    init {
        registerBluetoothIntentReceiver()
    }

    private var isActive = false

    var onDeviceFound: ((BluetoothDevice) -> Unit)? = null
    var onDiscoveryStateChanged: ((Boolean) -> Unit)? = null

    private fun registerBluetoothIntentReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        if (device != null) {
                            onDeviceFound?.invoke(device)
                        }
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        startDiscovery()
                        onDiscoveryStateChanged?.invoke(false)
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        setDiscoveryState()
                    }

                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state =
                            intent.getIntExtra(
                                BluetoothAdapter.EXTRA_STATE,
                                BluetoothAdapter.STATE_OFF
                            )
                        when (state) {
                            BluetoothAdapter.STATE_ON -> {
                                startDiscovery()
                            }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)

        context.registerReceiver(receiver, filter)
    }

    private fun setDiscoveryState() {
        var isDiscovering = false
        try {
            isDiscovering = bluetoothAdapter?.isDiscovering == true
        } catch (e: SecurityException) {
            Log.e(CLASS_NAME, "exception", e)
        }

        onDiscoveryStateChanged?.invoke(isDiscovering)
    }

    private fun showBluetoothRestartDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Bluetooth Issue")
            .setMessage("Unable to start discovery. Please try restarting Bluetooth.")
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun startDiscovery() {
        if (isActive) {
            try {
                if (bluetoothAdapter?.isEnabled == true) {
                    if (bluetoothAdapter?.isDiscovering == false) {
                        if (bluetoothAdapter?.startDiscovery() == false) {
                            showBluetoothRestartDialog(context)
                        }
                    }
                } else {
                    enableBluetooth()
                }
            } catch (e: SecurityException) {
                Log.e(CLASS_NAME, "exception", e)
            }
        }
    }

    private fun startBluetoothDiscoveryWithPermissionCheck() {
        if (checkPermissions()) {
            return startDiscovery()
        }
    }

    private fun cancelBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                if (bluetoothAdapter?.isDiscovering == true) {
                    bluetoothAdapter?.cancelDiscovery()
                }
            } catch (e: SecurityException) {
                Log.e(CLASS_NAME, "exception", e)
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return when {
            (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                    ) -> true

            (ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            )) -> {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_denied))
                    .setMessage(context.getString(R.string.bluetooth_scan_permission_explanation))
                    .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                        openAppSettings()
                    }.show()
                false
            }

            (ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            )) -> {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_denied))
                    .setMessage(context.getString(R.string.bluetooth_connect_permission_explanation))
                    .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                        openAppSettings()
                    }.show()
                false
            }

            else -> {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    REQUEST_BLUETOOTH_PERMISSION_FOR_DISCOVERY
                )
                false
            }

        }
    }

    fun setIsActive(isActive: Boolean) {
        this.isActive = isActive
        if (isActive) {
            startBluetoothDiscoveryWithPermissionCheck()
        } else {
            cancelBluetoothDiscovery()
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION_FOR_DISCOVERY) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startDiscovery()
            }
        }
    }

    fun destroy() {
        context.unregisterReceiver(receiver)
    }
}