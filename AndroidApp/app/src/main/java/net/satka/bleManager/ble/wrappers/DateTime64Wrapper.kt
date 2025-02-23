package net.satka.bleManager.ble.wrappers

import android.util.Log
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneOffset

open class DateTime64Wrapper : CharacteristicValueWrapper<LocalDateTime> {
    protected fun toBytes(value: LocalDateTime, bytesCount : Int): ByteArray {
        val timestamp = value.toEpochSecond(ZoneOffset.UTC)

        return ByteArray(bytesCount) { i ->
            ((timestamp shr (8 * i)) and 0xFF).toByte()
        }
    }

    override fun fromBytes(bytes: ByteArray): LocalDateTime {
            val timestamp = bytes.foldIndexed(0L) { index, acc, byte ->
                (acc or ((byte.toLong() and 0xFF) shl (index * 8)))
            }
        try {
            return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
        } catch (e: DateTimeException) {
            Log.e("DateTime64Wrapper", "DateTimeException: ${e.message}")
            return LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        }
    }

    override fun toBytes(value: LocalDateTime): ByteArray {
        return toBytes(value, 8)
    }
}

class DateTime64BigEndianWrapper : DateTime64Wrapper() {
    override fun fromBytes(bytes: ByteArray): LocalDateTime {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: LocalDateTime): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}