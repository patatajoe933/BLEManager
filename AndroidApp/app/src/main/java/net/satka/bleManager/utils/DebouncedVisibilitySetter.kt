package net.satka.bleManager.utils

import android.os.Handler
import android.os.Looper
import android.view.View

class DebouncedVisibilitySetter(
    private val delayMillis: Long,
    private vararg val views: View
) {

    private val handler = Handler(Looper.getMainLooper())
    private val showBusyIndicatorRunnable = Runnable {
        views.forEach { view ->
            view.visibility =
                if (isVisibleState) View.VISIBLE else View.INVISIBLE
        }
    }

    private var isVisibleState = false
    fun setIsVisible(isVisible: Boolean) {
        isVisibleState = isVisible
        // Zobrazit okamžitě, skrýt se zpožděním
        val realDelayMillis = if (isVisibleState) 0 else delayMillis
        handler.removeCallbacks(showBusyIndicatorRunnable)
        handler.postDelayed(
            showBusyIndicatorRunnable,
            realDelayMillis
        )
    }
}