package de.dimskiy.waypoints.platform.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.dimskiy.waypoints.platform.karooservices.KarooServiceProvider
import io.hammerhead.karooext.KarooSystemService
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KarooModule {

    @Provides
    @Singleton
    fun provideKarooServiceProvider(
        @ApplicationContext context: Context,
        @BaseModule.DispatcherDefault coroutinesDispatcher: CoroutineDispatcher
    ) = KarooServiceProvider(KarooSystemService(context))
}