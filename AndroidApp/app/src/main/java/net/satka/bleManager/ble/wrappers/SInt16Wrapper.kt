package net.satka.bleManager.ble.wrappers

open class SInt16Wrapper : CharacteristicValueWrapper<Short> {
    companion object {
        const val MIN_VALUE = Short.MIN_VALUE
        const val MAX_VALUE = Short.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): Short {
        if (bytes.isEmpty()) {
            return 0
        }

        val byte0 = bytes[0].toInt() and 0xFF
        val byte1 = if (bytes.size > 1) bytes[1].toInt() and 0xFF else 0x00

        return (byte0 or (byte1 shl 8)).toShort()
    }

    override fun toBytes(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            (value.toInt() shr 8 and 0xFF).toByte()
        )
    }
}

class SInt16BigEndianWrapper : SInt16Wrapper() {
    override fun fromBytes(bytes: ByteArray): Short {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: Short): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}