package net.satka.bleManager.ui

import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.satka.bleManager.R
import net.satka.bleManager.ble.models.DescriptorValueModel
import net.satka.bleManager.ble.models.DeviceModel
import net.satka.bleManager.ble.models.ServiceCharacteristicUUID
import net.satka.bleManager.ble.models.ServiceCharacteristicValueModel
import net.satka.bleManager.ble.models.setupDeviceModel
import net.satka.bleManager.ble.services.BluetoothDeviceConnectionService
import net.satka.bleManager.ble.services.BluetoothService
import net.satka.bleManager.data.db.AppDatabase
import net.satka.bleManager.data.model.Device
import net.satka.bleManager.databinding.ActivityDeviceDetailBinding
import net.satka.bleManager.ui.adapters.DevicePageAdapter
import net.satka.bleManager.ui.components.WebLinkDialog
import net.satka.bleManager.ui.models.DeviceViewModel
import net.satka.bleManager.utils.DebouncedEnabledSetter
import net.satka.bleManager.utils.DebouncedVisibilitySetter
import net.satka.bleManager.utils.Donation
import net.satka.bleManager.utils.InsetsUtil

class DeviceDetailActivity : AppCompatActivity() {
    companion object {
        private val CLASS_NAME = DeviceDetailActivity::class.java.name
        private const val TIMEOUT_DURATION_MILLIS = 8000L
        private const val SNACKS_DURATION_MILLIS = 5000
    }

    private var connectionHelpHasBeenActivated = false;
    private lateinit var binding: ActivityDeviceDetailBinding
    private lateinit var database: AppDatabase
    private var bleConnectionService: BluetoothDeviceConnectionService? = null
    private var deviceMacAddress: String? = null
    private var deviceModel: DeviceModel? = null
    private var tabLayoutMediator: TabLayoutMediator? = null
    private val deviceViewmodel: DeviceViewModel by viewModels()
    private var tabsAreValid = false
    private lateinit var debouncedVisibilitySetter: DebouncedVisibilitySetter
    private lateinit var debouncedEnabledSetter: DebouncedEnabledSetter
    private var recreate = false
    private val connectTimeoutScope = lifecycleScope
    private var connectTimeoutJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false
        enableEdgeToEdge()
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        debouncedVisibilitySetter = DebouncedVisibilitySetter(500, binding.progressIndicator)
        debouncedEnabledSetter = DebouncedEnabledSetter(
            500, binding.topAppBar.menu.findItem(R.id.device_refresh),
            binding.topAppBar.menu.findItem(R.id.data_refresh)
        )
        binding.topAppBar.setNavigationOnClickListener(::onToolbarNavigationBackClick)
        binding.topAppBar.setOnMenuItemClickListener(::onMenuItemClick)
        binding.viewPager.setOnApplyWindowInsetsListener(InsetsUtil::applyWindowsInsets)
        val address = intent.getStringExtra(getString(R.string.key_deviceaddress))
        deviceMacAddress = address
        database = AppDatabase.getDatabase(this)
        setupConnection(address)
    }

    private fun setupConnection(macAddress: String?) {
        if (macAddress != null) {
            if (bleConnectionService == null) {
                bleConnectionService =
                    BluetoothDeviceConnectionService(
                        this,
                        macAddress
                    )
                bleConnectionService?.onConnected = ::onDeviceConnected
                bleConnectionService?.onDisconnected =
                    ::onDeviceDisconnected
                bleConnectionService?.onServicesDiscovered =
                    ::onDeviceServicesDiscovered
                bleConnectionService?.onDescriptorsRead = ::onDescriptorsRead
                bleConnectionService?.onCharacteristicsRead =
                    ::onCharacteristicsRead
                bleConnectionService?.onCharacteristicsChanged =
                    ::onCharacteristicsChanged
                bleConnectionService?.onError = ::onConnectionError
                bleConnectionService?.onWorkStarted = ::onBluetoothWorkStarted
                bleConnectionService?.onWorkEnded = ::onBluetoothWorkEnded

                CoroutineScope(Dispatchers.IO).launch {
                    val dev = database.deviceDao().getDeviceByMac(macAddress)
                    withContext(Dispatchers.Main) {
                        bleConnectionService?.connectGatt(dev?.requestMTU == true)
                    }
                }
            }
        }
    }

    private fun showConnectionProblemsHelp(show: Boolean) {
        if (isDestroyed || isFinishing) {
            return
        }

        if (show) {
            if (!connectionHelpHasBeenActivated) {
                val connectionProblemsHelp =
                    getString(
                        R.string.connection_failed_help,
                        getString(R.string.doc_url_anchored)
                    ).trimIndent()

                binding.connectionProblemsHelpTextView.text =
                    HtmlCompat.fromHtml(connectionProblemsHelp, HtmlCompat.FROM_HTML_MODE_LEGACY)
                binding.connectionProblemsHelpTextView.movementMethod =
                    LinkMovementMethod.getInstance()
                binding.connectionProblemsHelpTextView.visibility = View.VISIBLE
            }
        } else {
            binding.connectionProblemsHelpTextView.visibility = View.GONE
        }

        connectionHelpHasBeenActivated = true
    }

    private fun startConnectionTimeoutJob() {
        connectTimeoutJob?.cancel()
        connectTimeoutJob = connectTimeoutScope.launch {
            var c = 0;
            while (!isDestroyed && !isFinishing) {
                delay(TIMEOUT_DURATION_MILLIS)
                if (!isDestroyed && !isFinishing) {
                    showConnectionProblemsHelp(true)
                    Snackbar.make(
                        binding.root,
                        getString(
                            if (++c % 3 == 0) {
                                R.string.not_responding_too_long
                            } else {
                                R.string.not_responding
                            }
                        ),
                        SNACKS_DURATION_MILLIS
                    ).setTextMaxLines(10).show()
                    delay(SNACKS_DURATION_MILLIS.toLong())
                }
            }
        }
    }

    private fun endConnectionTimeoutJob() {
        showConnectionProblemsHelp(false)
        connectTimeoutJob?.cancel()
        connectTimeoutJob = null
    }

    private fun invalidateDeviceVisually() {
        tabsAreValid = false
        binding.overlay.visibility = View.VISIBLE
    }

    private fun onDeviceDisconnected(gatt: BluetoothGatt?) {
        lifecycleScope.launch(Dispatchers.Main) {
            invalidateDeviceVisually()
            if (recreate) {
                recreate = false
                onBluetoothWorkStarted()
                //Musíme počkat, aby se nezrecyklovalo připojení. Neumím to jinak řešit.
                delay(BluetoothDeviceConnectionService.SECURE_DISCONNECTION_DELAY_MS)
                recreate()
            }
        }
    }

    private fun onDeviceConnected(gatt: BluetoothGatt?) {
        lifecycleScope.launch(Dispatchers.Main) {

        }
    }

    private fun onDeviceServicesDiscovered(gatt: BluetoothGatt?) {
        lifecycleScope.launch(Dispatchers.Main) {

        }
    }

    private fun onDescriptorsRead(
        gatt: BluetoothGatt?,
        descriptorValueModels: List<DescriptorValueModel>
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            deviceModel = createDeviceModel(gatt, descriptorValueModels, false)
        }
    }

    private fun onCharacteristicsChanged(
        gatt: BluetoothGatt?,
        serviceCharacteristicValueModel: ServiceCharacteristicValueModel
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            setCharacteristicsValue(deviceModel, serviceCharacteristicValueModel)
        }
    }

    private fun onCharacteristicsRead(
        gatt: BluetoothGatt?,
        serviceCharacteristicValueModels: List<ServiceCharacteristicValueModel>
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            setCharacteristicsValues(deviceModel, serviceCharacteristicValueModels)
            setupTabs(deviceModel)
        }
    }

    private fun setCharacteristicsValue(
        devModel: DeviceModel?,
        serviceCharacteristicValueModel: ServiceCharacteristicValueModel
    ) {
        devModel?.setCharacteristicValue(serviceCharacteristicValueModel)
    }

    private fun setCharacteristicsValues(
        devModel: DeviceModel?,
        serviceCharacteristicValueModels: List<ServiceCharacteristicValueModel>
    ) {
        devModel?.setCharacteristicValues(serviceCharacteristicValueModels)
    }

    private fun onConnectionError(status: Int, description: String) {
        Log.e(CLASS_NAME, "Connection error: $status, $description")
        lifecycleScope.launch(Dispatchers.Main) {
            Snackbar.make(
                binding.root,
                "${BluetoothService.getGattErrorMessage(status)}\n$description",
                if (description.isBlank()) SNACKS_DURATION_MILLIS else Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.dismiss)) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.try_reconnect),
                    SNACKS_DURATION_MILLIS
                ).setAction(getString(R.string.reconnect)) {
                    restartActivity(true)
                }.show()
            }.setTextMaxLines(10).show()
        }
    }

    private fun setupTabs(deviceModel: DeviceModel?) {
        if (!tabsAreValid) {
            binding.tabLayout.visibility = View.INVISIBLE
            deviceViewmodel.deviceModel = deviceModel
            val serviceUUIDs = deviceModel?.getServices()?.map { it.uuid } ?: listOf()
            if (serviceUUIDs.isNotEmpty()) {
                binding.tabLayout.visibility = View.VISIBLE
                tabsAreValid = true;
            }

            val pagerAdapter = DevicePageAdapter(this, serviceUUIDs)
            binding.viewPager.adapter = pagerAdapter

            tabLayoutMediator?.detach()
            tabLayoutMediator =
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    if (serviceUUIDs.size > position) {
                        tab.text = deviceModel?.getService(serviceUUIDs[position])?.name
                            ?: serviceUUIDs[position].toString()
                    }
                }

            tabLayoutMediator?.attach()
            binding.overlay.visibility = View.GONE
        }
    }

    //TODO: Ověřit jak je to s odmítnutím zápisu hodnoty na straně zařízení a zda není nutné přenačíst hodnotu charakteristiky.
    //Synchronizace hodnot by však dávala smysl pouze v režimu zápisu s potvrzením
    private fun onWriteCharacteristic(
        serviceUUID: java.util.UUID,
        characteristicUUID: java.util.UUID,
        data: ByteArray
    ) {
        bleConnectionService?.enqueueWriteToCharacteristic(serviceUUID, characteristicUUID, data)
    }

    private fun createDeviceModel(
        gatt: BluetoothGatt?,
        descriptorValueModels: List<DescriptorValueModel>,
        redOnly: Boolean
    ): DeviceModel? {
        if (gatt != null) {
            if (descriptorValueModels.isNotEmpty()) {
                return setupDeviceModel(
                    gatt,
                    descriptorValueModels,
                    ::onWriteCharacteristic,
                    redOnly
                )
            } else {
                WebLinkDialog.show(
                    this,
                    getString(R.string.no_matching_descriptor_found),
                    getString(
                        R.string.no_descriptor_found_message,
                        getString(R.string.doc_url)
                    ),
                    getString(R.string.doc_url)
                )
            }
        }

        return null
    }

    private fun onToolbarNavigationBackClick(view: View) {
        finish()
    }

    private fun openDeviceSettings() {
        tabsAreValid = false
        val intent = Intent(this, DeviceSettingsActivity::class.java)
        intent.putExtra(resources.getString(R.string.key_deviceaddress), deviceMacAddress)
        startActivity(intent)
    }

    private fun refreshData() {
        val serviceCharacteristicUUIDs = mutableListOf<ServiceCharacteristicUUID>()
        deviceModel?.getServices()?.forEach { service ->
            service.getCharacteristics().forEach { characteristic ->
                serviceCharacteristicUUIDs.add(object : ServiceCharacteristicUUID {
                    override val serviceUUID = service.uuid
                    override val characteristicUUID = characteristic.uuid
                })
            }
        }

        if (serviceCharacteristicUUIDs.isNotEmpty()) {
            bleConnectionService?.readCharacteristics(serviceCharacteristicUUIDs)
        }
    }

    private fun donate() {
        Donation.OpenDonationPage(this)
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.device_settings -> {
                openDeviceSettings()
                true
            }

            R.id.donate -> {
                donate()
                true
            }

            R.id.device_refresh -> {
                refreshServices()
                true
            }

            R.id.data_refresh -> {
                refreshData()
                true
            }

            else -> false
        }
    }

    private fun refreshServices() {
        tabsAreValid = false
        bleConnectionService?.refreshServices()
    }

    private fun onBluetoothWorkStarted() {
        lifecycleScope.launch(Dispatchers.Main) {
            startConnectionTimeoutJob()
            debouncedVisibilitySetter.setIsVisible(true)
            debouncedEnabledSetter.setIsEnabled(false)
        }
    }

    private fun onBluetoothWorkEnded() {
        lifecycleScope.launch(Dispatchers.Main) {
            endConnectionTimeoutJob()
            debouncedVisibilitySetter.setIsVisible(false)
            debouncedEnabledSetter.setIsEnabled(true)
        }
    }

    private fun restartActivity(forceImmediateDisconnect: Boolean = false) {
        invalidateDeviceVisually()
        if (bleConnectionService == null || forceImmediateDisconnect) {
            lifecycleScope.launch(Dispatchers.Main) {
                bleConnectionService?.disconnectActiveConnection(true)
                delay(BluetoothDeviceConnectionService.SECURE_DISCONNECTION_DELAY_MS)
                recreate()
            }
        } else {
            recreate = true
            bleConnectionService?.disconnectActiveConnection()
        }
    }

    private fun setDevice(macAddress: String?) {
        if (macAddress != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val dev = database.deviceDao().getDeviceByMac(macAddress)
                withContext(Dispatchers.Main) {
                    setTitle(dev)
                    if (dev != null) {
                        val connectionService = bleConnectionService
                        if (connectionService != null && connectionService.isReady && connectionService.requestMTU != dev.requestMTU) {
                            //Změna konfigurace MTU. Kompletní reconnect. Nejjednodušší způsob je restart aktivity
                            restartActivity()
                        } else {
                            bleConnectionService?.setDescriptorUUIDMask(dev.descriptorUUIDMask)
                        }
                    }
                }
            }
        }
    }

    private fun setTitle(device: Device?) {
        binding.topAppBar.title = if (device == null || device.name.isBlank()) {
            deviceMacAddress ?: ""
        } else {
            device.name
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        bleConnectionService?.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onResume() {
        super.onResume()
        setDevice(deviceMacAddress)
    }

    override fun onDestroy() {
        super.onDestroy()
        bleConnectionService?.destroy()
        endConnectionTimeoutJob()
    }

    override fun onStop() {
        super.onStop()
        tabsAreValid = false
    }
}
