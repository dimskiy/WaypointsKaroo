package de.dimskiy.waypoints.domain.providers.photonservice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.providers.WaypointsSearchProvider
import de.dimskiy.waypoints.model.LocalError
import de.dimskiy.waypoints.platform.di.BaseModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private const val RESULTS_NUMBER_LIMIT = 30
private const val SEARCH_MAP_ZOOM_LVL = 14
private const val LOCATION_BIAS_SCALE: Double = 0.0
private const val API_REQUEST_TIMEOUT_SEC = 20

class PhotonSearchProvider @Inject constructor(
    private val photonApiService: PhotonApiService,
    @ApplicationContext private val context: Context,
    @BaseModule.DispatcherIO private val coroutineDispatcher: CoroutineDispatcher
) : WaypointsSearchProvider {

    override suspend fun searchWaypoints(
        query: String,
        resultsLanguageCode: String
    ): DataResult<List<Waypoint.Discovered>> = withContext(coroutineDispatcher) {
        runCatchingWithTimeout {
            photonApiService.getFeaturedLocations(
                query = query,
                resultsLanguageCode = resultsLanguageCode,
                limit = RESULTS_NUMBER_LIMIT
            )
        }.mapResult { it.features.mapNotNull(::mapToWaypoint) }
    }

    override suspend fun searchWaypointsWithLocation(
        query: String,
        resultsLanguageCode: String,
        location: DeviceLocation
    ): DataResult<List<Waypoint.Discovered>> = withContext(coroutineDispatcher) {
        runCatchingWithTimeout {
            photonApiService.getFeaturedLocationsWithGeo(
                query = query,
                resultsLanguageCode = resultsLanguageCode,
                lat = location.latitude,
                lon = location.longitude,
                zoom = SEARCH_MAP_ZOOM_LVL,
                locationBiasScale = LOCATION_BIAS_SCALE,
                limit = RESULTS_NUMBER_LIMIT
            )
        }.mapResult { it.features.mapNotNull(::mapToWaypoint) }
    }

    private suspend fun <T> runCatchingWithTimeout(apiCall: suspend () -> T): DataResult<T> =
        try {
            withTimeout(API_REQUEST_TIMEOUT_SEC.seconds) {
                val response = apiCall()
                DataResult.ready(response)
            }
        } catch (e: TimeoutCancellationException) {
            DataResult.error(
                LocalError.NetworkError(message = context.getString(R.string.error_msg_network_timeout))
            )

        } catch (e: Exception) {
            DataResult.error(LocalError.NetworkError(e))
        }

    private fun mapToWaypoint(featureDto: FeatureDto): Waypoint.Discovered? {
        return if (featureDto.properties.serverId != null && featureDto.properties.name != null) {
            Waypoint.Discovered(
                serverId = featureDto.properties.serverId,
                name = featureDto.properties.name,
                address = with(featureDto.properties) {
                    Waypoint.Address(
                        country = country,
                        city = city,
                        zip = postcode,
                        street = street,
                        house = houseNumber,
                        qualifier1 = qualifier1,
                        qualifier2 = qualifier2,
                    )
                },
                latitude = featureDto.geometry.coordinates[1],
                longitude = featureDto.geometry.coordinates[0],
            )
        } else null
    }

    override fun getProviderName(): String = context.getString(R.string.photon_service_name)
}