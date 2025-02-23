package net.satka.bleManager.ui.models

data class KnownBluetoothDeviceModel (
    val name: String?,
    val address: String,
    val status: String,
    var isChecked: Boolean = false,
    var isEnabled: Boolean = true
)