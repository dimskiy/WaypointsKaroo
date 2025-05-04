package de.dimskiy.waypoints.platform.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.dimskiy.waypoints.domain.model.ReportingModel
import de.dimskiy.waypoints.domain.providers.ReportingProvider
import timber.log.Timber
import javax.inject.Inject

class FirebaseReportingProvider @Inject constructor(
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val firebaseAnalytics: FirebaseAnalytics
) : ReportingProvider {

    override fun report(model: ReportingModel): ReportingProvider {
        Timber.d("Analytics reporting: $model")

        val paramsBundle = model.params?.let { map ->
            val bundle = Bundle()
            map.forEach { (key, value) -> bundle.putString(key, value) }
            bundle
        }

        firebaseAnalytics.logEvent(model.key, paramsBundle)

        return this
    }

    override fun logError(exception: Throwable): ReportingProvider {
        Timber.w("Exception logging: $exception")

        firebaseCrashlytics.recordException(exception)

        return this
    }
}