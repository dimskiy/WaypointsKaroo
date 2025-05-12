package de.dimskiy.waypoints.platform.errordisplay.di

import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.dimskiy.waypoints.domain.UserInformerState
import de.dimskiy.waypoints.platform.errordisplay.UserInformerStateImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorDisplayModule {

    @Binds
    protected abstract fun bindErrorDisplay(errorDisplayState: UserInformerStateImpl): UserInformerState

    @Binds
    abstract fun bindErrorDisplayReceiver(errorDisplayState: UserInformerStateImpl): UserInformerState.Receiver

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Accessor {
        fun errorDisplayState(): UserInformerState
    }
}