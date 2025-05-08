package de.dimskiy.waypoints.platform.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.dimskiy.waypoints.domain.providers.ReportingProvider
import de.dimskiy.waypoints.platform.analytics.FirebaseReportingProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindReportingProvider(impl: FirebaseReportingProvider): ReportingProvider

    companion object {

        @Provides
        fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
            FirebaseAnalytics.getInstance(context)

        @Provides
        fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
    }
}