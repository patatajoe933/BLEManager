package net.satka.bleManager.ble.models

import java.util.UUID
interface ServiceCharacteristicUUID {
    val serviceUUID: UUID
    val characteristicUUID: UUID
}



open class CharacteristicValueModel(val characteristicUUID: UUID, val value: ByteArray)

class ServiceCharacteristicValueModel(override val serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray) :
    CharacteristicValueModel(characteristicUUID, value), ServiceCharacteristicUUID
