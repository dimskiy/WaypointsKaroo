package de.dimskiy.waypoints.platform.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.dimskiy.waypoints.domain.providers.photonservice.PhotonApiService
import de.dimskiy.waypoints.platform.network.KarooPhotonApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideKarooPhotonApiService(impl: KarooPhotonApiService): PhotonApiService = impl
}
