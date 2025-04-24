package de.dimskiy.waypoints.platform.ui.screens.waypointslist.model

import de.dimskiy.waypoints.DataResult

data class SearchResponse(
    val dataResult: DataResult<List<WaypointWithDistance>>,
    val providerName: String,
    val geoSearchEnabled: Boolean
)