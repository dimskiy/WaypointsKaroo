package de.dimskiy.waypoints.platform.ui.screens.waypointslist.model

import de.dimskiy.waypoints.domain.model.Waypoint

sealed class UserIntent {

    data class PerformSearch(val query: String) : UserIntent()

    data class ToggleBookmark(val model: Waypoint) : UserIntent()

    data class ToggleGeoSearch(val isEnabled: Boolean) : UserIntent()

    data class DismissWaypoint(val model: Waypoint) : UserIntent()

    data class ClickWaypoint(val model: Waypoint) : UserIntent()
}