package net.satka.bleManager.ble.wrappers

open class BooleanWrapper : CharacteristicValueWrapper<Boolean> {
    override fun fromBytes(bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) {
            return false
        }

        return bytes.any { it != 0.toByte() }
    }

    override fun toBytes(value: Boolean): ByteArray {
        return byteArrayOf(if (value) 1.toByte() else 0.toByte())
    }
}
