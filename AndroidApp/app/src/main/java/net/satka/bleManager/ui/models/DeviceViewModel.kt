package net.satka.bleManager.ui.models

import androidx.lifecycle.ViewModel
import net.satka.bleManager.ble.models.DeviceModel

class DeviceViewModel : ViewModel() {
    var deviceModel: DeviceModel? = null
}