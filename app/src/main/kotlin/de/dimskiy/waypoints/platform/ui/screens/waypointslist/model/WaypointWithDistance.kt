package de.dimskiy.waypoints.platform.ui.screens.waypointslist.model

import de.dimskiy.waypoints.domain.model.Waypoint

data class WaypointWithDistance(
    val distanceToDeviceKm: Double?,
    val waypoint: Waypoint
)