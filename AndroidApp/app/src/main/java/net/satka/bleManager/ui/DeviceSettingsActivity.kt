package net.satka.bleManager.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.satka.bleManager.R
import net.satka.bleManager.data.db.AppDatabase
import net.satka.bleManager.data.model.Device
import net.satka.bleManager.databinding.ActivityDeviceSettingsBinding

class DeviceSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceSettingsBinding
    private var deviceMacAddress: String? = null
    private lateinit var database: AppDatabase
    var device: Device? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        binding = ActivityDeviceSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.setNavigationOnClickListener(::onToolbarNavigationBackClick)

        deviceMacAddress = intent.getStringExtra(getString(R.string.key_deviceaddress))
        database = AppDatabase.getDatabase(this)
        setupListeners()
        loadDeviceDetails(deviceMacAddress)
    }

    override fun onPause() {
        super.onPause()
        saveDeviceDetails()
    }

    private fun saveDeviceDetails() {
        val dev = device
        if (dev != null) {
            val descriptorUUIDMask =
                if (binding.uuidInputLayout.error == null) {
                    binding.deviceDescriptorUuidMaskInput.text.toString().padEnd(32, '#')
                } else {
                    dev.descriptorUUIDMask
                }
            CoroutineScope(Dispatchers.IO).launch {
                database.deviceDao().updateDevice(
                    dev.copy(
                        name = binding.deviceNameInput.text.toString(),
                        descriptorUUIDMask = descriptorUUIDMask,
                        requestMTU = binding.requestMtuSwitch.isChecked
                    )
                )
            }
        }
    }

    private fun loadDeviceDetails(macAddress: String?) {
        if (macAddress != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val dev = database.deviceDao().getDeviceByMac(macAddress)
                device = dev
                withContext(Dispatchers.Main) {
                    if (dev != null) {
                        binding.deviceNameInput.setText(dev.name)
                        binding.deviceDescriptorUuidMaskInput.setText(dev.descriptorUUIDMask)
                        binding.requestMtuSwitch.isChecked = dev.requestMTU
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.deviceDescriptorUuidMaskInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                val formatted = formatUUID(input)
                if (validateUUID(input)) {
                    binding.uuidInputLayout.helperText =
                        getString(R.string.formatted_uuid_mask, formatted)
                    binding.uuidInputLayout.error = null
                } else {
                    binding.uuidInputLayout.helperText = null
                    binding.uuidInputLayout.error = getString(R.string.invalid_uuid_mask_format)
                }
            }

            private fun formatUUID(input: String): String {
                // Odstranění všech nepovolených znaků (kromě povolených hex a #)
                val sanitized = (input.filter { it.isDigit() || it in "abcdefABCDEF#" }).padEnd(32, '#')
                val result = StringBuilder()
                for (i in sanitized.indices) {
                    result.append(sanitized[i])
                    if ((i == 7 || i == 11 || i == 15 || i == 19) && i != sanitized.length - 1) {
                        result.append("-")
                    }
                }
                return result.toString()
            }

            private fun validateUUID(input: String): Boolean {
                val regex = "^(?!#+$)[0-9a-fA-F#]{1,32}$"
                return input.matches(regex.toRegex())
            }
        })
    }

    private fun onToolbarNavigationBackClick(view: View) {
        finish()
    }
}