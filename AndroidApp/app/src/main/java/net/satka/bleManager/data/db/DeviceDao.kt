package net.satka.bleManager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import net.satka.bleManager.data.model.Device

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device)

    @Update
    suspend fun updateDevice(device: Device)

    @Query("SELECT * FROM Device")
    suspend fun getAllDevices(): List<Device>

    @Query("SELECT * FROM Device WHERE macAddress = :macAddress")
    suspend fun getDeviceByMac(macAddress: String): Device?

    @Query("DELETE FROM Device WHERE macAddress = :macAddress")
    suspend fun deleteDeviceByMac(macAddress: String)
}
