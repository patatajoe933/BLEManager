package net.satka.bleManager.ble.wrappers

open class SInt8Wrapper : CharacteristicValueWrapper<Byte> {
    companion object {
        const val MIN_VALUE = Byte.MIN_VALUE
        const val MAX_VALUE = Byte.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): Byte {
        if (bytes.isEmpty()) {
            return 0
        }

        return bytes[0]
    }

    override fun toBytes(value: Byte): ByteArray {
        return byteArrayOf(value)
    }
}

class SInt8BigEndianWrapper : SInt8Wrapper() {
    override fun fromBytes(bytes: ByteArray): Byte {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: Byte): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}

