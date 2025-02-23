package net.satka.bleManager.ui

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.satka.bleManager.R
import net.satka.bleManager.ble.services.BluetoothDiscoveryService
import net.satka.bleManager.data.db.AppDatabase
import net.satka.bleManager.data.model.Device
import net.satka.bleManager.databinding.ActivityAddNewDeviceBinding
import net.satka.bleManager.ui.adapters.UnknownBluetoothDeviceAdapter
import net.satka.bleManager.ui.models.UnknownBluetoothDeviceModel
import net.satka.bleManager.utils.DebouncedVisibilitySetter
import net.satka.bleManager.utils.InsetsUtil

class AddNewDeviceActivity : AppCompatActivity() {
    companion object {
        private val CLASS_NAME = AddNewDeviceActivity::class.java.name
    }

    private lateinit var bluetoothDiscoveryService: BluetoothDiscoveryService
    private lateinit var database: AppDatabase
    private lateinit var debouncedVisibilitySetter: DebouncedVisibilitySetter
    private lateinit var binding: ActivityAddNewDeviceBinding
    private val devicesList = mutableListOf<UnknownBluetoothDeviceModel>()

    private val unknownBluetoothDeviceAdapter = UnknownBluetoothDeviceAdapter(devicesList)

    private fun startDiscovery() {
        bluetoothDiscoveryService.setIsActive(true)
    }

    private fun stopDiscovery() {
        bluetoothDiscoveryService.setIsActive(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false
        enableEdgeToEdge()

        binding = ActivityAddNewDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener(::onToolbarNavigationBackClick)

        binding.recyclerViewBluetoothDevices.setOnApplyWindowInsetsListener(InsetsUtil::applyWindowsInsets)
        binding.recyclerViewBluetoothDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBluetoothDevices.adapter = unknownBluetoothDeviceAdapter
        unknownBluetoothDeviceAdapter.onItemClick = ::onDeviceSelected

        debouncedVisibilitySetter = DebouncedVisibilitySetter(1000, binding.progressIndicator)
        bluetoothDiscoveryService = BluetoothDiscoveryService(this)
        bluetoothDiscoveryService.onDiscoveryStateChanged = debouncedVisibilitySetter::setIsVisible
        bluetoothDiscoveryService.onDeviceFound = ::onDeviceFound

        database = AppDatabase.getDatabase(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        bluetoothDiscoveryService.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun onToolbarNavigationBackClick(view: View) {
        finish()
    }

    private fun addToKnownDevices(macAddress: String, name: String?) {
        val deviceName = name ?: macAddress
        val context = this
        CoroutineScope(Dispatchers.IO).launch {
            database.deviceDao().insertDevice(
                Device(
                    macAddress, deviceName,
                    getString(R.string.default_descriptor_uuid_mask),
                    false
                )
            )

            withContext(Dispatchers.Main) {
                val intent = Intent(context, DeviceDetailActivity::class.java)
                intent.putExtra(resources.getString(R.string.key_devicename), deviceName)
                intent.putExtra(resources.getString(R.string.key_deviceaddress), macAddress)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun onDeviceFound(device: BluetoothDevice) {
        var deviceName: String? = getString(R.string.no_permissions)

        try {
            deviceName = device.name
        } catch (ex: SecurityException) {
            Log.e(CLASS_NAME, "exception", ex)
        }

        val deviceAddress = device.address
        if (deviceAddress != null) {
            CoroutineScope(Dispatchers.IO).launch {
                if (database.deviceDao().getDeviceByMac(deviceAddress) == null) {
                    withContext(Dispatchers.Main) {
                        val deviceIndex =
                            devicesList.indexOfFirst { it.address == deviceAddress }
                        if (deviceIndex == -1) {
                            devicesList.add(
                                UnknownBluetoothDeviceModel(
                                    deviceName, deviceAddress
                                )
                            )
                            unknownBluetoothDeviceAdapter.notifyItemInserted(devicesList.size - 1)
                        }
                    }
                }
            }
        }
    }

    private fun onDeviceSelected(device: UnknownBluetoothDeviceModel, position: Int) {
        //Pouze jeden click, pak přecházíme do jiné agendy
        unknownBluetoothDeviceAdapter.onItemClick = null
        stopDiscovery()
        addToKnownDevices(device.address, device.name)
    }

    override fun onResume() {
        super.onResume()
        startDiscovery()
    }

    override fun onPause() {
        super.onPause()
        stopDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothDiscoveryService.destroy()
    }
}