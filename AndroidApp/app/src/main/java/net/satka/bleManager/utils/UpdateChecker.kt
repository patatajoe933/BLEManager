package net.satka.bleManager.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.satka.bleManager.R
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object UpdateChecker {
    private fun isInstalledFromFdroid(context: Context): Boolean {
        return try {
            val packageName = context.packageName
            val installSourceInfo = context.packageManager.getInstallSourceInfo(packageName)
            return installSourceInfo.installingPackageName == "org.fdroid.fdroid"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun checkForUpdates(context: Context, onUpdateAvailable: (String, String) -> Unit) {
        if (isInstalledFromFdroid(context)) {
            return;
        }

        val client = OkHttpClient()
        val request = Request.Builder().url(context.getString(R.string.update_json_url)).build()

        withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: throw IllegalStateException("Response body is null")
                val json = JSONObject(responseBody)
                val latestVersion = json.getString("version")
                val downloadUrl = json.getString("url")

                val currentVersion = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName

                if (currentVersion != latestVersion) {
                    withContext(Dispatchers.Main){
                        onUpdateAvailable(latestVersion, downloadUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}