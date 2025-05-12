package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.model.ReportingModel
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.providers.LocationsProvider
import de.dimskiy.waypoints.domain.providers.ReportingProvider
import de.dimskiy.waypoints.domain.providers.SettingsProvider
import de.dimskiy.waypoints.domain.providers.WaypointsSearchProvider
import de.dimskiy.waypoints.domain.waypointsrepository.WaypointsRepository
import de.dimskiy.waypoints.model.LocalException
import de.dimskiy.waypoints.platform.di.BaseModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.Duration.Companion.minutes

class ObserveSearchResultsCase @Inject constructor(
    private val searchProvider: WaypointsSearchProvider,
    private val repository: WaypointsRepository,
    private val locationProvider: LocationsProvider,
    private val settingsProvider: SettingsProvider,
    private val reportingProvider: ReportingProvider,
    @BaseModule.TimeProvider private val currentTimeProvider: Provider<Long>
) {
    val providerName: String = searchProvider.getProviderName()

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        query: String,
        resultsLanguageCode: String
    ): Flow<DataResult<List<Waypoint>>> =
        settingsProvider.observeGeoSearchEnabled().flatMapLatest { isGeoSearchEnabled ->
            reportingProvider.report(ReportingModel.PerformSearch(isGeoSearchEnabled))

            if (isGeoSearchEnabled) {
                searchWithLocationFlow(query, resultsLanguageCode)
            } else {
                getSearchFlow {
                    searchProvider.searchWaypoints(query, resultsLanguageCode)
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun searchWithLocationFlow(
        query: String,
        resultsLanguageCode: String
    ): Flow<DataResult<List<Waypoint>>> {
        return settingsProvider.observeLastLocation().flatMapLatest { lastLocation ->
            if (isLastLocationObsolete(lastLocation)) {
                Timber.d("Location cache obsolete - discovering new...")
                locationProvider.observeDeviceLocations().onEach {
                    it.data?.let { settingsProvider.saveLocation(it) }
                }

            } else {
                Timber.d("Use cached location: $lastLocation")
                flowOf(DataResult.ready(lastLocation))
            }

        }.flatMapLatest { locationResult ->
            when (locationResult) {
                is DataResult.Error -> flowOf(DataResult.error(locationResult.error))

                is DataResult.Loading -> flowOf(locationResult)

                is DataResult.Ready -> locationResult.data?.let { deviceLocation ->
                    Timber.d("Performing geo-search...")
                    getSearchFlow {
                        searchProvider.searchWaypointsWithLocation(
                            query = query,
                            resultsLanguageCode = resultsLanguageCode,
                            location = deviceLocation
                        )
                    }
                } ?: flowOf(DataResult.error(LocalException.LocationServiceException()))
            }
        }
    }

    private fun isLastLocationObsolete(lastLocation: DeviceLocation?): Boolean {
        val timeDelta = currentTimeProvider.get() - (lastLocation?.timestamp ?: 0)
        return timeDelta >= LAST_LOCATION_OBSOLETE_TIMEOUT.inWholeMilliseconds
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getSearchFlow(getSearchResult: suspend () -> DataResult<List<Waypoint.Discovered>>): Flow<DataResult<List<Waypoint>>> =
        channelFlow {
            send(DataResult.loading())

            val waypointsDiscoveredResult = getSearchResult()
            reportingProvider.report(
                ReportingModel.SearchApiResult(
                    isSuccess = waypointsDiscoveredResult.isReady(),
                    errorMessage = waypointsDiscoveredResult.getErrorIfAny()?.message
                )
            )

            repository.observeWaypointsStored().mapLatest { itemsStored ->
                val itemsStoredMap = itemsStored.associateBy(Waypoint::serverId)
                waypointsDiscoveredResult.mapResult { items ->
                    items.map { itemsStoredMap[it.serverId] ?: it }
                }
            }.collectLatest(::send)

            awaitCancellation()
        }

    companion object {
        val LAST_LOCATION_OBSOLETE_TIMEOUT = 10.minutes
    }
}