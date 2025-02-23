package net.satka.bleManager.ble.wrappers

open class FloatWrapper : CharacteristicValueWrapper<Float> {
    companion object {
        const val MIN_VALUE = -Float.MAX_VALUE
        const val MAX_VALUE = Float.MAX_VALUE
    }

    override fun fromBytes(bytes: ByteArray): Float {
        if (bytes.size != 4) {
            return  Float.NaN
        }

        val intBits = bytes.foldIndexed(0) { index, acc, byte -> (acc or ((byte.toInt() and 0xFF) shl (index * 8))) }
        return Float.fromBits(intBits)
    }

    override fun toBytes(value: Float): ByteArray {
        val intBits = value.toBits()
        return byteArrayOf(
            (intBits and 0xFF).toByte(),
            (intBits shr 8 and 0xFF).toByte(),
            (intBits shr 16 and 0xFF).toByte(),
            (intBits shr 24 and 0xFF).toByte()
        )
    }
}

class FloatBigEndianWrapper : FloatWrapper(){
    override fun fromBytes(bytes: ByteArray): Float {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: Float): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}