package net.satka.bleManager.ble.wrappers

open class UInt32Wrapper : CharacteristicValueWrapper<UInt> {
    companion object {
        const val MIN_VALUE = UInt.MIN_VALUE
        const val MAX_VALUE = UInt.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): UInt {
        if (bytes.isEmpty()) {
            return 0u
        }

        val byte0 = bytes[0].toUInt() and 0xFFu
        val byte1 = if (bytes.size > 1) bytes[1].toUInt() and 0xFFu else 0u
        val byte2 = if (bytes.size > 2) bytes[2].toUInt() and 0xFFu else 0u
        val byte3 = if (bytes.size > 3) bytes[3].toUInt() and 0xFFu else 0u

        return (byte0 or (byte1 shl 8) or (byte2 shl 16) or (byte3 shl 24))
    }

    override fun toBytes(value: UInt): ByteArray {
        return byteArrayOf(
            (value and 0xFFu).toByte(),
            (value shr 8 and 0xFFu).toByte(),
            (value shr 16 and 0xFFu).toByte(),
            (value shr 24 and 0xFFu).toByte()
        )
    }
}

class UInt32BigEndianWrapper : UInt32Wrapper() {
    override fun fromBytes(bytes: ByteArray): UInt {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: UInt): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}