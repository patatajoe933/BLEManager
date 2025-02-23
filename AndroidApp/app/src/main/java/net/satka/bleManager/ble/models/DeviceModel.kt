package net.satka.bleManager.ble.models

import android.bluetooth.BluetoothGatt
import android.util.Log
import java.util.UUID

data class DeviceModel(private val services: Map<UUID, ServiceModel>) {
    fun setCharacteristicValue(serviceCharacteristicValueModel: ServiceCharacteristicValueModel) {
        services[serviceCharacteristicValueModel.serviceUUID]?.setCharacteristicValue(
            serviceCharacteristicValueModel
        )
    }

    fun setCharacteristicValues(serviceCharacteristicValueModels: List<ServiceCharacteristicValueModel>) {
        serviceCharacteristicValueModels.forEach { setCharacteristicValue(it) }
    }

    fun getServices() : List<ServiceModel> {
        return services.values.sortedBy { it.name }.sortedBy { it.order }
    }

    fun getService(uuid: UUID) : ServiceModel? {
        return services[uuid]
    }
}

fun setupDeviceModel(
    gatt: BluetoothGatt,
    descriptorValueModels: List<DescriptorValueModel>,
    onWriteCharacteristic: (serviceUUID: UUID, characteristicUUID: UUID, data: ByteArray) -> Unit,
    readOnly: Boolean
): DeviceModel {
    return DeviceModel(
        gatt.services.associate { service ->
            Log.d("Device model", "creating service: ${service.uuid}")
            service.uuid to setupServiceModel(
                service,
                descriptorValueModels,
                onWriteCharacteristic,
                readOnly
            )
        }.filterValues { it != null }
            .mapValues { it.value!! })
}