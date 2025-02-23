package net.satka.bleManager.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import net.satka.bleManager.R

object WebLinkDialog {
    var activeDialog: AlertDialog? = null
    fun show(context: Context, title: String, message: String, url: String) {
        activeDialog?.dismiss()
        activeDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(context.getString(R.string.copy_link)) { _, _ ->
                copyToClipboard(context, url)
            }
            .setPositiveButton(context.getString(R.string.open)) { dialog, _ ->
                openWebPage(context, url)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.copied_link), text)
        clipboard.setPrimaryClip(clip)
    }

    private fun openWebPage(context: Context, url: String) {
        try {
            val webpage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("WebLinkDialog", "Open failed: ${e.message}")
            showError(context, url)
        }
    }

    private fun showError(context: Context, url: String) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.unable_to_open_link))
            .setMessage(context.getString(R.string.open_the_link_in_your_browser, url))
            .setNeutralButton(context.getString(R.string.copy_link)) { _, _ ->
                copyToClipboard(context, url)
            }
            .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}