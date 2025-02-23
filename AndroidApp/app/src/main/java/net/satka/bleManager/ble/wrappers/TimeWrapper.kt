package net.satka.bleManager.ble.wrappers
import java.time.LocalTime
import java.time.temporal.ChronoField

open class TimeWrapper : CharacteristicValueWrapper<LocalTime> {
    override fun fromBytes(bytes: ByteArray): LocalTime {
        var  secondsSinceStartOfDay = bytes.foldIndexed(0L) { index, acc, byte ->
                acc or ((byte.toLong() and 0xFFL) shl (index * 8))
            }

        // Zajistíme validní rozsah (0 až 86399 sekund)
        if (secondsSinceStartOfDay !in 0L..86399L) {
            secondsSinceStartOfDay = 0L;
        }

        ChronoField.SECOND_OF_DAY.checkValidValue(secondsSinceStartOfDay)

        return LocalTime.ofSecondOfDay(secondsSinceStartOfDay)
    }

    override fun toBytes(value: LocalTime): ByteArray {
        val secondsSinceStartOfDay = value.toSecondOfDay()

        return ByteArray(4) { i ->
            ((secondsSinceStartOfDay shr (8 * i)) and 0xFF).toByte()
        }
    }
}

class TimeBigEndianWrapper : TimeWrapper() {
    override fun fromBytes(bytes: ByteArray): LocalTime {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: LocalTime): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}
