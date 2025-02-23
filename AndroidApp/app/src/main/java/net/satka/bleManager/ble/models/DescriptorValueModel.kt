package net.satka.bleManager.ble.models

import net.satka.bleManager.ble.configuration.DescriptorConfigurationParser
import java.util.UUID

class DescriptorValueModel (val serviceUUID: UUID, val characteristicUUID: UUID, val value: ByteArray) {
    var configuration : DescriptorConfiguration? = null
        private set
    var errorText: String? = null
        private set

    init {
        try {
            configuration = DescriptorConfigurationParser.parseJson(String(value))
        } catch (ex: Exception) {
            errorText = ex.message
        }
    }
}