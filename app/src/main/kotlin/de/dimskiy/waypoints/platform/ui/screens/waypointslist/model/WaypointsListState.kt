package de.dimskiy.waypoints.platform.ui.screens.waypointslist.model

import de.dimskiy.waypoints.domain.model.Waypoint

data class WaypointsListState(
    val bookmarks: List<Waypoint>,
    val searchResponse: SearchResponse
)