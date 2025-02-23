package net.satka.bleManager.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import net.satka.bleManager.R
import net.satka.bleManager.databinding.ActivityAboutBinding


class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.setNavigationOnClickListener(::onToolbarNavigationBackClick)
        setLicensesText()
    }

    private fun setLicensesText() {
        val licenseText = getString(R.string.license_text).trimIndent()

        binding.textView.text = HtmlCompat.fromHtml(licenseText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun onToolbarNavigationBackClick(view: View) {
        finish()
    }
}