package net.satka.bleManager.ble.wrappers

open class UInt16Wrapper : CharacteristicValueWrapper<UShort> {
    companion object {
        const val MIN_VALUE = UShort.MIN_VALUE
        const val MAX_VALUE = UShort.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): UShort {
        if (bytes.isEmpty()) {
            return 0u
        }

        val byte0 = bytes[0].toUInt() and 0xFFu
        val byte1 = if (bytes.size > 1) bytes[1].toUInt() and 0xFFu else 0u

        return (byte0 or (byte1 shl 8)).toUShort()
    }

    override fun toBytes(value: UShort): ByteArray {
        return byteArrayOf(
            (value.toUInt() and 0xFFu).toByte(),
            (value.toUInt() shr 8 and 0xFFu).toByte()
        )
    }
}

class UInt16BigEndianWrapper : UInt16Wrapper() {
    override fun fromBytes(bytes: ByteArray): UShort {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: UShort): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}