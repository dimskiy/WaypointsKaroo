package de.dimskiy.waypoints.platform

import android.content.Context
import androidx.startup.Initializer
import de.dimskiy.waypoints.BuildConfig
import timber.log.Timber

class LoggingInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}