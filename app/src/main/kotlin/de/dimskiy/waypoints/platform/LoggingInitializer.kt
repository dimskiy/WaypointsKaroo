package de.dimskiy.waypoints.platform

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.dimskiy.waypoints.BuildConfig
import timber.log.Timber

class LoggingInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    class CrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, exception: Throwable?) {
            if (priority < Log.WARN) return

            FirebaseCrashlytics.getInstance().log("$tag: $message")

            if (exception != null && priority >= Log.ERROR) {
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        }
    }
}