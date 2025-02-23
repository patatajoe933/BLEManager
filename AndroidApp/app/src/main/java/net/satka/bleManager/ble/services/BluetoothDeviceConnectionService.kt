package net.satka.bleManager.ble.services

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import net.satka.bleManager.R
import net.satka.bleManager.ble.models.DescriptorValueModel
import net.satka.bleManager.ble.models.ServiceCharacteristicUUID
import net.satka.bleManager.ble.models.ServiceCharacteristicValueModel
import net.satka.bleManager.constants.PermissionRequests.REQUEST_BLUETOOTH_PERMISSIONS_FOR_CONNECT
import net.satka.bleManager.utils.BLEWriteQueue
import net.satka.bleManager.utils.Comparers
import java.util.LinkedList
import java.util.UUID

class BluetoothDeviceConnectionService(
    context: ComponentActivity,
    private val macAddress: String
) :
    BluetoothService(context) {
    companion object {
        const val SECURE_DISCONNECTION_DELAY_MS = 4000L;
        private val CLASS_NAME = BluetoothDeviceConnectionService::class.java.name
        const val GATT_MAX_ATTR_LEN = 512
        const val NOTIFICATION_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"
        const val NO_2902_DESCRIPTOR = -1
        const val NO_PROPERTY_READ = -2
        const val DESCRIPTOR_READ_FAILED = -3
        const val NO_PROPERTY_WRITE = -4
        const val WRITE_ERROR = -5
        const val UNKNOWN_ERROR = -6
        const val NOT_CONNECTED = -7
    }

    init {
        registerBluetoothIntentReceiver()
    }

    private var descriptorUUIDMask: String? = null
    private lateinit var receiver: BroadcastReceiver
    private val loadDescriptorQueue: LinkedList<BluetoothGattDescriptor> =
        LinkedList<BluetoothGattDescriptor>()
    private val loadCharacteristicQueue: LinkedList<BluetoothGattCharacteristic> =
        LinkedList<BluetoothGattCharacteristic>()
    private val enableNotificationsCharacteristicQueue: LinkedList<BluetoothGattCharacteristic> =
        LinkedList<BluetoothGattCharacteristic>()
    private val bleWriteQueue = BLEWriteQueue(::writeToToCharacteristic)
    private var loadedDescriptors: MutableList<DescriptorValueModel> = mutableListOf()
    private var loadedCharacteristics: MutableList<ServiceCharacteristicValueModel> =
        mutableListOf()

    var activeConnection: BluetoothGatt? = null
        private set
    var requestMTU: Boolean = false
        private set
    var isReady = false
        private set
    var mtuRequested = false

    var onConnected: ((BluetoothGatt?) -> Unit)? = null
    var onDisconnected: ((BluetoothGatt?) -> Unit)? = null
    var onServicesDiscovered: ((BluetoothGatt?) -> Unit)? = null
    var onError: ((Int, String) -> Unit)? = null
    var onDescriptorsRead: ((gatt: BluetoothGatt, descriptorValueModels: List<DescriptorValueModel>) -> Unit)? =
        null
    var onCharacteristicsRead: ((gatt: BluetoothGatt, characteristicModels: List<ServiceCharacteristicValueModel>) -> Unit)? =
        null
    var onCharacteristicsChanged: ((gatt: BluetoothGatt, characteristicModel: ServiceCharacteristicValueModel) -> Unit)? =
        null
    var onWorkStarted: (() -> Unit)? = null
    var onWorkEnded: (() -> Unit)? = null

    private fun registerBluetoothIntentReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state =
                            intent.getIntExtra(
                                BluetoothAdapter.EXTRA_STATE,
                                BluetoothAdapter.STATE_OFF
                            )
                        when (state) {
                            BluetoothAdapter.STATE_ON -> {
                                connectGatt(requestMTU)
                            }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(receiver, filter)
    }

    private fun invokeOnFatalError(status: Int, description: String) {
        onWorkEnded?.invoke()
        onError?.invoke(status, description)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.d("TEST", status.toString() + "" + newState.toString())
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(CLASS_NAME, "STATE_CONNECTED")
                        onConnected?.invoke(gatt)
                        try {
                            if (!requestMTU || gatt?.requestMtu(517) != true) {
                                isReady = true
                                discoverServices(gatt)
                            } else {
                                mtuRequested = true;
                            }
                        } catch (e: SecurityException) {
                            Log.e(CLASS_NAME, "exception", e)
                        }
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        isReady = false
                        try {
                            gatt?.close()
                            activeConnection = null
                        } catch (e: SecurityException) {
                            Log.e(CLASS_NAME, "exception", e)
                        }

                        onDisconnected?.invoke(gatt)
                        onWorkEnded?.invoke()
                    }
                }
            } else {
                Log.e(CLASS_NAME, "UNABLE TO CONNECT")
                invokeOnFatalError(status, "")
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    isReady = false
                    //Zařízení se odpojilo, ale autoconnect by měl zařídit opětovné připojení
                    onDisconnected?.invoke(gatt)
                    if (status == 133) {
                        try {
                            gatt?.close()
                            activeConnection = null
                        } catch (e: SecurityException) {
                            Log.e(CLASS_NAME, "exception", e)
                        }
                    } else {
                        onWorkStarted?.invoke()
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (mtuRequested) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //Chvíli po ukončení spojení a opětovném připojení se callback volá dvakrát. Asi chyba v Androidu
                    mtuRequested = false
                    isReady = true
                    discoverServices(gatt)
                    Log.d("BLE", "MTU changed to: $mtu")
                } else {
                    invokeOnFatalError(status,
                        context.getString(R.string.mtu_size_negotiation_failed))
                    Log.e("BLE", "MTU change failed: $status")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(CLASS_NAME, "SERVICES DISCOVERED")
                readAllDescriptors(gatt)
                onServicesDiscovered?.invoke(gatt)
            } else {
                Log.e(CLASS_NAME, "UNABLE TO DISCOVER SERVICES")
                invokeOnFatalError(status, context.getString(R.string.service_discovery_failed))
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            super.onDescriptorRead(gatt, descriptor, status, value)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                loadedDescriptors.add(
                    DescriptorValueModel(
                        descriptor.characteristic.service.uuid,
                        descriptor.characteristic.uuid,
                        value
                    )
                )
            } else {
                Log.e(CLASS_NAME, "UNABLE TO READ DESCRIPTOR")
                onError?.invoke(status, descriptor.characteristic.uuid.toString())
            }

            readNextDescriptor(gatt)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                loadedCharacteristics.add(
                    ServiceCharacteristicValueModel(
                        characteristic.service.uuid,
                        characteristic.uuid,
                        value
                    )
                )
            } else {
                Log.e(CLASS_NAME, "UNABLE TO READ CHARACTERISTIC")
                onError?.invoke(status, characteristic.uuid.toString())
            }

            readNextCharacteristic(gatt)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            onCharacteristicsChanged?.invoke(
                gatt, ServiceCharacteristicValueModel(
                    characteristic.service.uuid,
                    characteristic.uuid,
                    value
                )
            )
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (descriptor.uuid.toString() == NOTIFICATION_DESCRIPTOR) {
                    enableNextNotifications(gatt)
                }
            } else {
                invokeOnFatalError(status, descriptor.characteristic.uuid.toString())
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onWorkEnded?.invoke()
            } else {
                invokeOnFatalError(status, characteristic.uuid.toString())
            }

            bleWriteQueue.onWriteComplete()
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS_FOR_CONNECT) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                connectGatt(requestMTU)
            }
        }
    }

    private fun enableNotifications(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ): Boolean {
        val notificationsAllowed =
            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
        val indicationsAllowed =
            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
        if (notificationsAllowed || indicationsAllowed) {
            val descriptor =
                characteristic.getDescriptor(UUID.fromString(NOTIFICATION_DESCRIPTOR))
            if (descriptor != null) {
                try {
                    gatt.setCharacteristicNotification(characteristic, true)
                    if (indicationsAllowed) {
                        gatt.writeDescriptor(
                            descriptor,
                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        )
                    } else {
                        gatt.writeDescriptor(
                            descriptor,
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        )
                    }

                    return true
                } catch (e: SecurityException) {
                    Log.e(CLASS_NAME, "exception", e)
                }
            } else {
                onError?.invoke(NO_2902_DESCRIPTOR, characteristic.uuid.toString())
            }
        }

        return false
    }

    private fun enableNextNotifications(
        gatt: BluetoothGatt
    ) {
        var notificationsEnabled: Boolean
        do {
            val characteristic = enableNotificationsCharacteristicQueue.poll()
            if (characteristic != null) {
                notificationsEnabled = enableNotifications(gatt, characteristic)
            } else {
                readAllCharacteristics(gatt)
                return
            }
        } while (!notificationsEnabled)
    }

    private fun createLoadQueues(gatt: BluetoothGatt) {
        gatt.services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                for (descriptor in characteristic.descriptors) {
                    if (Comparers.compareUUIDWithMask(descriptor.uuid, descriptorUUIDMask)) {
                        loadDescriptorQueue.add(descriptor)
                        loadCharacteristicQueue.add(characteristic)
                        enableNotificationsCharacteristicQueue.add(characteristic)
                        break
                    }
                }
            }
        }
    }

    private fun readNextDescriptor(gatt: BluetoothGatt) {
        val nextDescriptor = loadDescriptorQueue.poll()
        if (nextDescriptor != null) {
            try {
                if (!gatt.readDescriptor(nextDescriptor)) {
                    onError?.invoke(
                        DESCRIPTOR_READ_FAILED,
                        nextDescriptor.characteristic.uuid.toString()
                    )
                    readNextDescriptor(gatt)
                    return
                }
            } catch (e: SecurityException) {
                Log.e(CLASS_NAME, "exception", e)
            }
        } else {
            onDescriptorsRead?.invoke(gatt, loadedDescriptors)
            enableNextNotifications(gatt)
        }
    }

    private fun readAllDescriptors(gatt: BluetoothGatt) {
        loadedDescriptors.clear()
        createLoadQueues(gatt)
        readNextDescriptor(gatt)
    }

    private fun readNextCharacteristic(gatt: BluetoothGatt) {
        val nextCharacteristic = loadCharacteristicQueue.poll()
        if (nextCharacteristic != null) {
            try {
                if (!gatt.readCharacteristic(nextCharacteristic)) {
                    onError?.invoke(NO_PROPERTY_READ, nextCharacteristic.uuid.toString())
                    readNextCharacteristic(gatt)
                    return
                }
            } catch (e: SecurityException) {
                Log.e(CLASS_NAME, "exception", e)
            }
        } else {
            onCharacteristicsRead?.invoke(gatt, loadedCharacteristics)
            onWorkEnded?.invoke()
        }
    }

    private fun readAllCharacteristics(gatt: BluetoothGatt?) {
        if (gatt != null) {
            loadedCharacteristics.clear()
            readNextCharacteristic(gatt)
        }
    }

    private fun checkPermissions(): Boolean {
        return when {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
                -> true

            (ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            )) -> {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_denied))
                    .setMessage(context.getString(R.string.bluetooth_connect_permission_explanation))
                    .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                    }.show()
                false
            }

            else -> {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    REQUEST_BLUETOOTH_PERMISSIONS_FOR_CONNECT
                )
                false
            }

        }
    }

    fun disconnectActiveConnection(closeImidiately: Boolean = false ) {
        if (activeConnection != null) {
            try {
                isReady = false
                activeConnection?.disconnect()
                if (closeImidiately) {
                    activeConnection?.close()
                    activeConnection = null
                }
            } catch (e: SecurityException) {
                Log.e(CLASS_NAME, "exception", e)
            }
        }
    }

    private fun getWriteTypeForCharacteristic(characteristic: BluetoothGattCharacteristic): Int? {
        val properties = characteristic.properties
        return when {
            properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0 -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }

            properties and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE > 0 -> {
                BluetoothGattCharacteristic.WRITE_TYPE_SIGNED
            }

            properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0 -> {
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }

            else -> {
                return null
            }
        }
    }

    private fun writeToToCharacteristic(
        serviceUUID: UUID,
        characteristicUUID: UUID,
        data: ByteArray
    ): Boolean {
        activeConnection?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)?.let {
            val writeType = getWriteTypeForCharacteristic(it)
            if (writeType != null) {
                try {
                    try {
                        val res = activeConnection?.writeCharacteristic(
                            it,
                            data,
                            writeType
                        )

                        if (res == BluetoothStatusCodes.SUCCESS) {
                            onWorkStarted?.invoke()
                            return true
                        } else {
                            invokeOnFatalError(WRITE_ERROR, res.toString())
                        }

                    } catch (e: SecurityException) {
                        Log.e(CLASS_NAME, "exception", e)
                    }
                } catch (e: IllegalArgumentException) {
                    invokeOnFatalError(WRITE_ERROR, e.message ?: "")
                }
            } else {
                invokeOnFatalError(NO_PROPERTY_WRITE, characteristicUUID.toString())
            }
        }

        return false
    }

    private fun getMatchingCharacteristics(
        gatt: BluetoothGatt?,
        requiredItems: List<ServiceCharacteristicUUID>
    ): List<BluetoothGattCharacteristic> {
        val matchingCharacteristics = mutableListOf<BluetoothGattCharacteristic>()
        if (gatt != null) {
            for (service in gatt.services) {
                val matchedItems = requiredItems.filter { it.serviceUUID == service.uuid }
                for (item in matchedItems) {
                    val characteristic = service.getCharacteristic(item.characteristicUUID)
                    if (characteristic != null) {
                        matchingCharacteristics.add(characteristic)
                    }
                }
            }
        }

        return matchingCharacteristics
    }


    fun enqueueWriteToCharacteristic(serviceUUID: UUID, characteristicUUID: UUID, data: ByteArray) {
        bleWriteQueue.enqueueWrite(serviceUUID, characteristicUUID, data);
    }

    fun refreshServices() {
        if (activeConnection != null && isReady) {
            try {
                // BluetoothGatt instance
                val refreshMethod = activeConnection?.javaClass?.getMethod("refresh")
                refreshMethod?.invoke(activeConnection)
                onWorkStarted?.invoke()
                Handler(Looper.getMainLooper()).postDelayed({
                    onWorkStarted?.invoke() // Toto je tady kvůli timeoutu v Device detail activity. Bez toho by vyskakovalo upozornění, že zařízení neodpovídá.
                    discoverServices(activeConnection)
                }, 6000)
            } catch (e: Exception) {
                invokeOnFatalError(UNKNOWN_ERROR, e.message ?: "")
                Log.e(CLASS_NAME, "exception", e)
            }
        } else {
            invokeOnFatalError(NOT_CONNECTED, "Device not connected")
        }
    }

    fun connectGatt(requestMTU: Boolean) {
        if (activeConnection != null) {
            //Pouze jedno aktivní připojení
            return
        }
        this.requestMTU = requestMTU
        if (bluetoothAdapter?.isEnabled == true) {
            bluetoothAdapter?.getRemoteDevice(macAddress)?.let { device ->
                try {
                    activeConnection = device.connectGatt(
                        context,
                        true,
                        gattCallback,
                        BluetoothDevice.TRANSPORT_LE
                    )
                    onWorkStarted?.invoke()
                } catch (e: SecurityException) {
                    Log.e(CLASS_NAME, "exception", e)
                    activeConnection = null
                    checkPermissions()
                }
            }
        } else {
            enableBluetooth()
        }
    }

    fun readCharacteristics(characteristicModels: List<ServiceCharacteristicUUID>) {
        if (isReady) {
            val requiredCharacteristics =
                getMatchingCharacteristics(activeConnection, characteristicModels)
            if (requiredCharacteristics.isNotEmpty()) {
                loadCharacteristicQueue.addAll(requiredCharacteristics)
                onWorkStarted?.invoke()
                readAllCharacteristics(activeConnection)
            }
        }
    }

    fun discoverServices(gatt: BluetoothGatt?) {
        try {
            if (descriptorUUIDMask != null && isReady) {
                if (gatt?.discoverServices() == false) {
                    invokeOnFatalError(
                        UNKNOWN_ERROR,
                        context.getString(R.string.unable_to_discover_services)
                    )
                }
            } else {
                onWorkEnded?.invoke()
            }
        } catch (e: SecurityException) {
            Log.e(CLASS_NAME, "exception", e)
            checkPermissions()
        }
    }

    fun setDescriptorUUIDMask(descriptorUUIDMask: String) {
        if (descriptorUUIDMask != this.descriptorUUIDMask) {
            this.descriptorUUIDMask = descriptorUUIDMask
            if (activeConnection != null && isReady) {
                discoverServices(activeConnection)
                onWorkStarted?.invoke()
            }
        }
    }

    fun destroy() {
        context.unregisterReceiver(receiver)
        disconnectActiveConnection(true)
    }
}