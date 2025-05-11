package de.dimskiy.waypoints.platform.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.dimskiy.waypoints.domain.ErrorDisplayState
import de.dimskiy.waypoints.platform.errordisplay.ErrorDisplayStateImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorDisplayModule {

    @Binds
    abstract fun bindErrorDisplay(errorDisplayState: ErrorDisplayStateImpl): ErrorDisplayState

    @Binds
    abstract fun bindErrorDisplayReceiver(errorDisplayState: ErrorDisplayStateImpl): ErrorDisplayState.Receiver
}