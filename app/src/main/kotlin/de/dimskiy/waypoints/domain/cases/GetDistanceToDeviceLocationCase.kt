package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.providers.EnvironmentPropertiesProvider
import de.dimskiy.waypoints.domain.providers.SettingsProvider
import de.dimskiy.waypoints.platform.di.BaseModule
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.WaypointWithDistance
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0

class GetDistanceToDeviceLocationCase @Inject constructor(
    private val settingsProvider: SettingsProvider,
    private val propertiesProvider: EnvironmentPropertiesProvider,
    @BaseModule.DispatcherDefault private val coroutineDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(waypoints: List<Waypoint>): List<WaypointWithDistance> =
        withContext(coroutineDispatcher) {
            val isDistanceMetric = propertiesProvider.isMeasureUnitMetric()
            val deviceLastLocation = settingsProvider.observeLastLocation().first()
                ?: return@withContext waypoints.map {
                    WaypointWithDistance(
                        distanceToDeviceKm = null,
                        isDistanceMetric = isDistanceMetric,
                        waypoint = it
                    )
                }

            waypoints.map { waypoint ->
                val distance = getGreatCircleDistanceKm(
                    lat1 = waypoint.latitude,
                    lon1 = waypoint.longitude,
                    lat2 = deviceLastLocation.latitude,
                    lon2 = deviceLastLocation.longitude
                )
                Timber.d("Distance ($distance) for $waypoint")

                WaypointWithDistance(
                    distanceToDeviceKm = distance,
                    isDistanceMetric = isDistanceMetric,
                    waypoint = waypoint
                )
            }
        }

    private fun getGreatCircleDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val lat1 = Math.toRadians(lat1)
        val lat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }
}