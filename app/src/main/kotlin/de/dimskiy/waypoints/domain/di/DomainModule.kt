package de.dimskiy.waypoints.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.dimskiy.waypoints.domain.providers.EnvironmentPropertiesProvider
import de.dimskiy.waypoints.domain.providers.LocationsProvider
import de.dimskiy.waypoints.domain.providers.WaypointsSearchProvider
import de.dimskiy.waypoints.domain.providers.photonservice.PhotonSearchProvider
import de.dimskiy.waypoints.domain.waypointsrepository.WaypointsRepository
import de.dimskiy.waypoints.domain.waypointsrepository.WaypointsRepositoryImpl
import de.dimskiy.waypoints.platform.karooservices.KarooLocationsProvider
import de.dimskiy.waypoints.platform.karooservices.KarooPropertiesProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindWaypointsRepository(impl: WaypointsRepositoryImpl): WaypointsRepository

    @Binds
    @Singleton
    abstract fun bindWaypointsSearchProvider(impl: PhotonSearchProvider): WaypointsSearchProvider

    @Binds
    @Singleton
    abstract fun bindKarooLocationsProvider(impl: KarooLocationsProvider): LocationsProvider

    @Binds
    @Singleton
    abstract fun bindEnvironmentPropertiesProvider(impl: KarooPropertiesProvider): EnvironmentPropertiesProvider
}