package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.domain.model.ReportingModel
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.providers.ReportingProvider
import de.dimskiy.waypoints.domain.waypointsrepository.WaypointsRepository
import javax.inject.Inject

class ToggleWaypointBookmarkCase @Inject constructor(
    private val repository: WaypointsRepository,
    private val reportingProvider: ReportingProvider,
) {
    suspend operator fun invoke(model: Waypoint) {
        when(model) {
            is Waypoint.Stored -> {
                reportingProvider.report(ReportingModel.SearchItemDeleted)
                repository.deleteWaypointStored(model)
            }
            is Waypoint.Discovered -> {
                reportingProvider.report(ReportingModel.SearchItemBookmarked)
                repository.setWaypointStored(model)
            }
        }
    }
}