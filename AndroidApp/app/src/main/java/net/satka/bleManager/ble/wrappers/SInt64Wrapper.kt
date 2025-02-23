package net.satka.bleManager.ble.wrappers

open class SInt64Wrapper : CharacteristicValueWrapper<Long> {
    companion object {
        const val MIN_VALUE = Long.MIN_VALUE
        const val MAX_VALUE = Long.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): Long {
        if (bytes.isEmpty()) {
            return 0L
        }

        val byte0 = bytes[0].toLong() and 0xFF
        val byte1 = if (bytes.size > 1) bytes[1].toLong() and 0xFF else 0x00
        val byte2 = if (bytes.size > 2) bytes[2].toLong() and 0xFF else 0x00
        val byte3 = if (bytes.size > 3) bytes[3].toLong() and 0xFF else 0x00
        val byte4 = if (bytes.size > 4) bytes[4].toLong() and 0xFF else 0x00
        val byte5 = if (bytes.size > 5) bytes[5].toLong() and 0xFF else 0x00
        val byte6 = if (bytes.size > 6) bytes[6].toLong() and 0xFF else 0x00
        val byte7 = if (bytes.size > 7) bytes[7].toLong() and 0xFF else 0x00

        return (byte0 or (byte1 shl 8) or (byte2 shl 16) or (byte3 shl 24)
                or (byte4 shl 32) or (byte5 shl 40) or (byte6 shl 48) or (byte7 shl 56))
    }

    override fun toBytes(value: Long): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            (value shr 8 and 0xFF).toByte(),
            (value shr 16 and 0xFF).toByte(),
            (value shr 24 and 0xFF).toByte(),
            (value shr 32 and 0xFF).toByte(),
            (value shr 40 and 0xFF).toByte(),
            (value shr 48 and 0xFF).toByte(),
            (value shr 56 and 0xFF).toByte()
        )
    }
}

class SInt64BigEndianWrapper : SInt64Wrapper() {
    override fun fromBytes(bytes: ByteArray): Long {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: Long): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}