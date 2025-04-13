package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.waypointsrepository.WaypointsRepository
import javax.inject.Inject

class ToggleWaypointBookmarkCase @Inject constructor(
    private val repository: WaypointsRepository
) {
    suspend operator fun invoke(model: Waypoint) {
        when(model) {
            is Waypoint.Stored -> repository.deleteWaypointStored(model)
            is Waypoint.Discovered -> repository.setWaypointStored(model)
        }
    }
}