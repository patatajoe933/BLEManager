package net.satka.bleManager.ble.wrappers

open class SInt32Wrapper : CharacteristicValueWrapper<Int> {
    companion object {
        const val MIN_VALUE = Int.MIN_VALUE
        const val MAX_VALUE = Int.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): Int {
        if (bytes.isEmpty()) {
            return 0
        }

        val byte0 = bytes[0].toInt() and 0xFF
        val byte1 = if (bytes.size > 1) bytes[1].toInt() and 0xFF else 0x00
        val byte2 = if (bytes.size > 2) bytes[2].toInt() and 0xFF else 0x00
        val byte3 = if (bytes.size > 3) bytes[3].toInt() and 0xFF else 0x00

        return (byte0 or (byte1 shl 8) or (byte2 shl 16) or (byte3 shl 24))
    }

    override fun toBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            (value shr 8 and 0xFF).toByte(),
            (value shr 16 and 0xFF).toByte(),
            (value shr 24 and 0xFF).toByte()
        )
    }
}

class SInt32BigEndianWrapper : SInt32Wrapper() {
    override fun fromBytes(bytes: ByteArray): Int {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: Int): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}
