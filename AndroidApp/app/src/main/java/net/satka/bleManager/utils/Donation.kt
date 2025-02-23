package net.satka.bleManager.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.satka.bleManager.R

object Donation {
    fun OpenDonationPage(context: Context) {
        val donateUrl = context.getString(R.string.donate_link)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(donateUrl))
        context.startActivity(intent)
    }
}