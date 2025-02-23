package net.satka.bleManager.ble.wrappers

interface CharacteristicValueWrapper<T> {
    fun fromBytes(bytes: ByteArray): T
    fun toBytes(value: T): ByteArray
}
