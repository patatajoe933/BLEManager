package net.satka.bleManager.utils

import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import net.satka.bleManager.R

object InsetsUtil {
    fun applyWindowsInsets(view: View, windowInsets: WindowInsets): WindowInsets {
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
                    or WindowInsetsCompat.Type.ime()
        )
        view.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
        return WindowInsets.CONSUMED
    }

    fun applyWindowsInsetsIncludeFAB(
        view: View,
        windowInsets: WindowInsets
    ): WindowInsets {
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
                    or WindowInsetsCompat.Type.ime()
        )
        view.updatePadding(
            left = insets.left,
            right = insets.right,
            bottom = insets.bottom + view.resources.getDimensionPixelSize(R.dimen.fab_add_device_bottom_margin)
        )
        return WindowInsets.CONSUMED
    }

    fun applyWindowsInsetsFAB(view: View, windowInsets: WindowInsets): WindowInsets {
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
        )

        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = insets.left
            bottomMargin =
                insets.bottom + view.resources.getDimensionPixelSize(R.dimen.fab_add_device_bottom_margin)
            rightMargin =
                insets.right + view.resources.getDimensionPixelSize(R.dimen.fab_add_device_right_margin)
        }
        return WindowInsets.CONSUMED
    }
}