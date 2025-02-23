package net.satka.bleManager.ble.models

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import java.util.UUID

class ServiceModel(
    val uuid: UUID,
    private val characteristics: Map<UUID, CharacteristicModel<*>>,
) {
    val name: String
        get() {
            return characteristics.values.filterIsInstance<CharacteristicModel.ServiceName>()
                .firstOrNull()?.value ?: uuid.toString().take(8)
        }

    val order: Int
        get() {
            return characteristics.values.filterIsInstance<CharacteristicModel.ServiceName>()
                .firstOrNull()?.order ?: 0
        }

    fun setCharacteristicValue(serviceCharacteristicValueModel: CharacteristicValueModel) {
        characteristics[serviceCharacteristicValueModel.characteristicUUID]?.setByteValue(
            serviceCharacteristicValueModel.value
        )
    }

    fun getCharacteristics(): List<CharacteristicModel<*>> {
        return characteristics.values.sortedBy { it.uuid }.sortedBy { it.order }
    }
}

fun setupServiceModel(
    service: BluetoothGattService,
    descriptorValueModels: List<DescriptorValueModel>,
    onWriteCharacteristic: (serviceUUID: UUID, characteristicUUID: UUID, data: ByteArray) -> Unit,
    readOnly: Boolean
): ServiceModel? {
    val onWrite = { characteristicUUID: UUID, data: ByteArray ->
        onWriteCharacteristic(service.uuid, characteristicUUID, data)
    }

    val characteristics = service.characteristics.associate { characteristic ->
        Log.d("Service model", "Creating characteristic: ${characteristic.uuid}")
        characteristic.uuid to setupCharacteristicModel(
            characteristic.uuid,
            descriptorValueModels.firstOrNull { it.serviceUUID == service.uuid && it.characteristicUUID == characteristic.uuid },
            onWrite,
            !readOnly && ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                    (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0 ||
                    (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0)
        )
    }.filterValues { it != null }
        .mapValues { it.value!! }

    if (characteristics.isNotEmpty()) {
        return ServiceModel(
            service.uuid,
            characteristics
        )
    }

    return null
}
