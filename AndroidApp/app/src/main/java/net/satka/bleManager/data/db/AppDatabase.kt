package net.satka.bleManager.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.satka.bleManager.data.model.Device

@Database(entities = [Device::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao

    companion object {
        // Volatile zajišťuje, že instance bude dostupná ve všech threadech
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Funkce pro získání instance databáze
        fun getDatabase(context: Context): AppDatabase {
            // Používáme synchronized, aby byla zajištěna bezpečnost vláken
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .enableMultiInstanceInvalidation()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
