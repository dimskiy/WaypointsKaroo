package de.dimskiy.waypoints.domain.providers

import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.model.Waypoint

interface WaypointsSearchProvider {

    suspend fun searchWaypoints(query: String): DataResult<List<Waypoint.Discovered>>

    suspend fun searchWaypointsWithLocation(
        query: String,
        location: DeviceLocation
    ): DataResult<List<Waypoint.Discovered>>

    fun getProviderName(): String

}