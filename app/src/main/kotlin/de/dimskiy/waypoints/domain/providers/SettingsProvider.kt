package de.dimskiy.waypoints.domain.providers

import de.dimskiy.waypoints.domain.model.DeviceLocation
import kotlinx.coroutines.flow.Flow

interface SettingsProvider {

    fun observeLastLocation(): Flow<DeviceLocation?>

    suspend fun saveLocation(lastLocation: DeviceLocation)

    fun observeGeoSearchEnabled(): Flow<Boolean>

    suspend fun setGeoSearchEnabled(isEnabled: Boolean)
}