package net.satka.bleManager.utils

import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View

class DebouncedEnabledSetter(
    private val delayMillis: Long,
    private vararg val targets: Any
) {

    init {
        // Kontrola typů při vytvoření instance
        targets.forEach { target ->
            if (target !is View && target !is MenuItem) {
                throw IllegalArgumentException("Unsupported target type: ${target::class.simpleName}")
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateEnabledStateRunnable = Runnable {
        targets.forEach { target ->
            when (target) {
                is View -> target.isEnabled = isEnabledState
                is MenuItem -> target.isEnabled = isEnabledState
            }
        }
    }

    private var isEnabledState = true
    fun setIsEnabled(isEnabled: Boolean) {
        isEnabledState = isEnabled
        // Zakázat okamžitě, povolit ze spošděním
        val realDelayMillis = if (!isEnabledState) 0 else delayMillis
        handler.removeCallbacks(updateEnabledStateRunnable)
        handler.postDelayed(
            updateEnabledStateRunnable,
            realDelayMillis
        )
    }
}

