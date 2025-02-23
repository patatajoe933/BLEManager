package net.satka.bleManager.ble.wrappers

import android.util.Half

open class HalfFloatWrapper : CharacteristicValueWrapper<Float> {
    companion object {
        val MIN_VALUE = Half(Half.LOWEST_VALUE).toFloat()
        val MAX_VALUE = Half(Half.MAX_VALUE).toFloat()
    }

    override fun fromBytes(bytes: ByteArray): Float {
        if (bytes.size != 2) {
            return Float.NaN
        }

        // Vytvoření 16bitové hodnoty z bajtů (little endian)
        val halfBits: Short = ((bytes[1].toInt() and 0xFF) shl 8 or (bytes[0].toInt() and 0xFF)).toShort()

        // Převedení na Half a následně na Float
        @Suppress("HalfFloat")
        return Half.toFloat(halfBits)
    }

    override fun toBytes(value: Float): ByteArray {
        @Suppress("HalfFloat")
        val halfBits: Short = Half.toHalf(value)

        // Rozdělení na dva bajty v little endian formátu
        val lowByte = (halfBits.toInt() and 0xFF).toByte() // Nižších 8 bitů
        val highByte = ((halfBits.toInt() shr 8) and 0xFF).toByte() // Vyšších 8 bitů

        return byteArrayOf(lowByte, highByte)
    }
}

class HalfFloatBigEndianWrapper : HalfFloatWrapper() {
    override fun fromBytes(bytes: ByteArray): Float {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: Float): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}
