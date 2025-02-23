package net.satka.bleManager.ble.wrappers

open class UInt8Wrapper : CharacteristicValueWrapper<UByte> {
    companion object {
        const val MIN_VALUE = UByte.MIN_VALUE
        const val MAX_VALUE = UByte.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): UByte {
        if (bytes.isEmpty()) {
            return 0u
        }

        return bytes[0].toUByte()
    }

    override fun toBytes(value: UByte): ByteArray {
        return byteArrayOf(value.toByte())
    }
}

class UInt8BigEndianWrapper : UInt8Wrapper() {
    override fun fromBytes(bytes: ByteArray): UByte {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: UByte): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}

