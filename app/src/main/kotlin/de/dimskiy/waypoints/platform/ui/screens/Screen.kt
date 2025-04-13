package de.dimskiy.waypoints.platform.ui.screens

sealed class Screen(val route: String) {

    data object WaypointsList : Screen("WaypointsList")
}