package net.satka.bleManager.data.preferences

//import android.content.Context
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.booleanPreferencesKey
//import androidx.datastore.preferences.preferencesDataStore
//
//val CLEAR_CACHE_BEFORE_CONNECT = booleanPreferencesKey("clear_gatt_before_connect")
//
//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

//suspend fun saveClearCacheBeforeConnectValue(context: Context, value: Boolean) {
//    context.dataStore.edit { preferences ->
//        preferences[CLEAR_CACHE_BEFORE_CONNECT] = value
//    }
//}
//
//fun readClearCacheBeforeConnectValue(context: Context): Flow<Boolean?> {
//    return context.dataStore.data
//        .map { preferences ->
//            preferences[CLEAR_CACHE_BEFORE_CONNECT]
//        }
//}


//        lifecycleScope.launch {
//            readClearCacheBeforeConnectValue(this@AppSettingsActivity).collectLatest { isChecked ->
//                binding.clearCacheBeforeConnectSwitch.isChecked = isChecked ?: false
//            }
//        }
//
//        binding.clearCacheBeforeConnectSwitch.setOnCheckedChangeListener { _, isChecked ->
//            lifecycleScope.launch {
//                saveClearCacheBeforeConnectValue(this@AppSettingsActivity, isChecked)
//            }
//        }