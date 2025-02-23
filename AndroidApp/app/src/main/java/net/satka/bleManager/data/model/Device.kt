package net.satka.bleManager.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Device(
    @PrimaryKey val macAddress: String,
    val name: String,
    val descriptorUUIDMask: String,
    val requestMTU : Boolean
)