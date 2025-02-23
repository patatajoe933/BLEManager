package net.satka.bleManager.ble.wrappers

open class UInt64Wrapper : CharacteristicValueWrapper<ULong> {
    companion object {
        const val MIN_VALUE = ULong.MIN_VALUE
        const val MAX_VALUE = ULong.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): ULong {
        if (bytes.isEmpty()) {
            return 0uL
        }

        val byte0 = bytes[0].toULong() and 0xFFu
        val byte1 = if (bytes.size > 1) bytes[1].toULong() and 0xFFu else 0uL
        val byte2 = if (bytes.size > 2) bytes[2].toULong() and 0xFFu else 0uL
        val byte3 = if (bytes.size > 3) bytes[3].toULong() and 0xFFu else 0uL
        val byte4 = if (bytes.size > 4) bytes[4].toULong() and 0xFFu else 0uL
        val byte5 = if (bytes.size > 5) bytes[5].toULong() and 0xFFu else 0uL
        val byte6 = if (bytes.size > 6) bytes[6].toULong() and 0xFFu else 0uL
        val byte7 = if (bytes.size > 7) bytes[7].toULong() and 0xFFu else 0uL

        return (byte0 or (byte1 shl 8) or (byte2 shl 16) or (byte3 shl 24)
                or (byte4 shl 32) or (byte5 shl 40) or (byte6 shl 48) or (byte7 shl 56))
    }

    override fun toBytes(value: ULong): ByteArray {
        return byteArrayOf(
            (value and 0xFFu).toByte(),
            (value shr 8 and 0xFFu).toByte(),
            (value shr 16 and 0xFFu).toByte(),
            (value shr 24 and 0xFFu).toByte(),
            (value shr 32 and 0xFFu).toByte(),
            (value shr 40 and 0xFFu).toByte(),
            (value shr 48 and 0xFFu).toByte(),
            (value shr 56 and 0xFFu).toByte()
        )
    }
}

class UInt64BigEndianWrapper : UInt64Wrapper() {
    override fun fromBytes(bytes: ByteArray): ULong {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: ULong): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}