package net.satka.bleManager.ble.wrappers

class StringWrapper : CharacteristicValueWrapper<String> {
    override fun fromBytes(bytes: ByteArray): String {
        return String(bytes)
    }

    override fun toBytes(value: String): ByteArray {
        return value.toByteArray()
    }

}