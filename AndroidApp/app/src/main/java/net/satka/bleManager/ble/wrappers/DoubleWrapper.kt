package net.satka.bleManager.ble.wrappers

open class DoubleWrapper : CharacteristicValueWrapper<Double> {
    companion object {
        const val MIN_VALUE = -Double.MAX_VALUE
        const val MAX_VALUE = Double.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): Double {
        if (bytes.size != 8) {
            return Double.NaN
        }

        val longBits = bytes.foldIndexed(0L) { index, acc, byte -> (acc or ((byte.toLong() and 0xFF) shl (index * 8))) }
        return Double.fromBits(longBits)
    }

    override fun toBytes(value: Double): ByteArray {
        val longBits = value.toBits()
        return byteArrayOf(
            (longBits and 0xFF).toByte(),
            (longBits shr 8 and 0xFF).toByte(),
            (longBits shr 16 and 0xFF).toByte(),
            (longBits shr 24 and 0xFF).toByte(),
            (longBits shr 32 and 0xFF).toByte(),
            (longBits shr 40 and 0xFF).toByte(),
            (longBits shr 48 and 0xFF).toByte(),
            (longBits shr 56 and 0xFF).toByte()
        )
    }
}

class DoubleBigEndianWrapper : DoubleWrapper() {
    override fun fromBytes(bytes: ByteArray): Double {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: Double): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}