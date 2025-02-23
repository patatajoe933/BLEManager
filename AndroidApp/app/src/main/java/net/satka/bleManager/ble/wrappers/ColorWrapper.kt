package net.satka.bleManager.ble.wrappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

class ColorWrapper : CharacteristicValueWrapper<Color> {
    override fun fromBytes(bytes: ByteArray): Color {
        if (bytes.isEmpty()) {
            return Color.Black
        }

        val byte0 = bytes[0].toInt() and 0xFF
        val byte1 = if (bytes.size > 1) bytes[1].toInt() and 0xFF else 0
        val byte2 = if (bytes.size > 2) bytes[2].toInt() and 0xFF else 0
        val byte3 = if (bytes.size > 3) bytes[3].toInt() and 0xFF else 0xFF

        return Color(red = byte0, green = byte1, blue = byte2, alpha = byte3)
    }

    override fun toBytes(value: Color): ByteArray {
        val argb = value.toArgb()

        return byteArrayOf(
            ((argb shr 16) and 0xFF).toByte(),
            ((argb shr 8) and 0xFF).toByte(),
            (argb and 0xFF).toByte(),
            ((argb shr 24) and 0xFF).toByte()
        )
    }
}