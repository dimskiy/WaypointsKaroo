package de.dimskiy.waypoints.platform.ui.screens.waypointslist.model

data class SearchParams(
    val query: String,
    val withNearbyWaypointsFilter: Boolean
)