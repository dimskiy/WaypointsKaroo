package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.waypointsrepository.WaypointsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWaypointsStoredCase @Inject constructor(
    private val repository: WaypointsRepository
){
    operator fun invoke(): Flow<List<Waypoint>> = repository.observeWaypointsStored()
}