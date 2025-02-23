package net.satka.bleManager.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.satka.bleManager.R
import net.satka.bleManager.data.db.AppDatabase
import net.satka.bleManager.databinding.ActivityDeviceListBinding
import net.satka.bleManager.ble.services.BluetoothBondingService
import net.satka.bleManager.ble.services.BluetoothDeviceConnectionService
import net.satka.bleManager.ui.adapters.KnownBluetoothDeviceAdapter
import net.satka.bleManager.ui.components.WebLinkDialog
import net.satka.bleManager.ui.models.KnownBluetoothDeviceModel
import net.satka.bleManager.utils.Donation
import net.satka.bleManager.utils.InsetsUtil
import net.satka.bleManager.utils.UpdateChecker

class DeviceListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceListBinding
    private lateinit var database: AppDatabase

    private val devicesList = mutableListOf<KnownBluetoothDeviceModel>()
    private val knownBluetoothDeviceAdapter = KnownBluetoothDeviceAdapter(devicesList)
    private lateinit var bondingService: BluetoothBondingService
    private var selectedDeviceAddress : String? = null

    private fun clearSelection() {
        devicesList.forEachIndexed { index, item ->
            if (item.isChecked) {
                item.isChecked = false
                knownBluetoothDeviceAdapter.notifyItemChanged(index)
            }
        }

        setDeleteButtonVisibility()
    }

    private fun onDeviceSelected(device: KnownBluetoothDeviceModel, position: Int) {
        selectedDeviceAddress = device.address
        val intent = Intent(this, DeviceDetailActivity::class.java)
        intent.putExtra(resources.getString(R.string.key_devicename), device.name)
        intent.putExtra(resources.getString(R.string.key_deviceaddress), device.address)
        startActivity(intent)
    }

    private fun onDeviceCheckedChanged(device: KnownBluetoothDeviceModel, position: Int) {
        setDeleteButtonVisibility()
    }

    private fun deleteCheckedDevices() {
        devicesList.toList().forEachIndexed { _, item ->
            if (item.isChecked) {
                deleteDevice(item.address)
            }
        }
    }

    private fun deleteDevice(macAddress: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.deviceDao().deleteDeviceByMac(macAddress)
            withContext(Dispatchers.Main) {
                val position = devicesList.indexOfFirst { it.address == macAddress }
                devicesList.removeAt(position)
                knownBluetoothDeviceAdapter.notifyItemRemoved(position)
                setDeleteButtonVisibility()
                showEmptyDeviceListText(devicesList.isEmpty())
            }
        }
    }

    private fun onDeviceDeleteClick(device: KnownBluetoothDeviceModel, position: Int) {
        deleteDevice(device.address)
    }

    private fun setDeleteButtonVisibility() {
        binding.topAppBar.menu.findItem(R.id.delete).isVisible = devicesList.any { it.isChecked }
    }

    private fun onAddNewClick(view: View) {
        val intent = Intent(this, AddNewDeviceActivity::class.java)
        startActivity(intent)
    }

    fun showUpdateDialog(context: Context, downloadUrl: String) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.update_available))
            .setMessage(getString(R.string.update_dialog_content))
            .setPositiveButton(getString(R.string.update)) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(downloadUrl)
                context.startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun checkForUpdates() {
        val context = this
        lifecycleScope.launch {
            UpdateChecker.checkForUpdates(context) { latestVersion, downloadUrl ->
                showUpdateDialog(context, downloadUrl)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        clearSelection()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false
        enableEdgeToEdge()
        binding = ActivityDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerViewBluetoothDevices.setOnApplyWindowInsetsListener(InsetsUtil::applyWindowsInsetsIncludeFAB)
        binding.floatingAddNewDeviceButton.setOnApplyWindowInsetsListener(InsetsUtil::applyWindowsInsetsFAB)
        binding.floatingAddNewDeviceButton.setOnClickListener(::onAddNewClick)
        binding.recyclerViewBluetoothDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBluetoothDevices.adapter = knownBluetoothDeviceAdapter
        knownBluetoothDeviceAdapter.onItemClick = ::onDeviceSelected
        knownBluetoothDeviceAdapter.onItemCheckedChanged = ::onDeviceCheckedChanged
        knownBluetoothDeviceAdapter.onItemDeleteClick = ::onDeviceDeleteClick

        binding.topAppBar.setOnMenuItemClickListener(::onMenuItemClick)
        database = AppDatabase.getDatabase(this)
        bondingService = BluetoothBondingService(this)

        checkForUpdates()
    }

    private fun showEmptyDeviceListText(show : Boolean) {
        if (show) {
            val emptyDeviceListHelp = getString(
                R.string.empty_device_list_text,
                getString(R.string.doc_url_anchored),
                getString(R.string.doc_url_anchored),
                getString(R.string.doc_url)
            ).trimIndent()

            binding.emptyDeviceListTextView.text =
                HtmlCompat.fromHtml(emptyDeviceListHelp, HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.emptyDeviceListTextView.movementMethod = LinkMovementMethod.getInstance()
            binding.emptyDeviceListTextView.visibility = View.VISIBLE
        } else {
            binding.emptyDeviceListTextView.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun populateDeviceList() {
        CoroutineScope(Dispatchers.IO).launch {
            val devices = database.deviceDao().getAllDevices()
            withContext(Dispatchers.Main) {
                devicesList.clear()
                devicesList.addAll(devices.map {
                    KnownBluetoothDeviceModel(
                        it.name, it.macAddress,
                        if (bondingService.getBondState(it.macAddress) == BluetoothDevice.BOND_BONDED) {
                            getString(R.string.bonded)
                        } else {
                            getString(R.string.not_bonded)
                        },
                        isEnabled = selectedDeviceAddress != it.macAddress
                    )
                })

                knownBluetoothDeviceAdapter.notifyDataSetChanged()
                showEmptyDeviceListText(devicesList.isEmpty())
                selectedDeviceAddress = null
                delay(BluetoothDeviceConnectionService.SECURE_DISCONNECTION_DELAY_MS)
                devicesList.forEachIndexed { index, item ->
                    if (!item.isEnabled) {
                        item.isEnabled = true
                        knownBluetoothDeviceAdapter.notifyItemChanged(index)
                    }
                }
            }
        }
    }

    private fun openAbout() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    private fun openDeveloperGuide() {
        WebLinkDialog.show(
            this,
            getString(R.string.developer_guide),
            getString(R.string.doc_url),
            getString(R.string.doc_url)
        )
    }

    private fun donate() {
        Donation.OpenDonationPage(this)
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                deleteCheckedDevices()
                true
            }

            R.id.developer_guide -> {
                openDeveloperGuide()
                true
            }

            R.id.donate -> {
                donate()
                true
            }

            R.id.about -> {
                openAbout()
                true
            }

            else -> false
        }
    }

    override fun onResume() {
        super.onResume()
        populateDeviceList()
    }
}