package de.dimskiy.waypoints.domain.waypointsrepository

import de.dimskiy.waypoints.domain.model.Waypoint
import kotlinx.coroutines.flow.Flow

interface WaypointsRepository {

    fun observeWaypointsStored(): Flow<List<Waypoint.Stored>>

    suspend fun getWaypointStored(serverId: String): Waypoint.Stored?

    suspend fun setWaypointStored(waypoint: Waypoint)

    suspend fun deleteWaypointStored(waypoint: Waypoint.Stored)

}