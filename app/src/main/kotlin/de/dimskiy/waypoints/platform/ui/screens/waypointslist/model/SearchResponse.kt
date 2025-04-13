package de.dimskiy.waypoints.platform.ui.screens.waypointslist.model

import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.domain.model.Waypoint

data class SearchResponse(
    val dataResult: DataResult<List<Waypoint>>,
    val providerName: String,
    val geoSearchEnabled: Boolean
)