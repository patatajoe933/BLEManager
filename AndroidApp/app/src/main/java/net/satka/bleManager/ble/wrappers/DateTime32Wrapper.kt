package net.satka.bleManager.ble.wrappers

import java.time.LocalDateTime

open class DateTime32Wrapper : DateTime64Wrapper() {
    override fun toBytes(value: LocalDateTime): ByteArray {
        return toBytes(value, 4)
    }
}

class DateTime32BigEndianWrapper : DateTime32Wrapper() {
    override fun fromBytes(bytes: ByteArray): LocalDateTime {
        return super.fromBytes(bytes.reversedArray())
    }

    override fun toBytes(value: LocalDateTime): ByteArray {
        return super.toBytes(value).reversedArray()
    }
}