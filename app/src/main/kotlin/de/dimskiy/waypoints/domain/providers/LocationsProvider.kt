package de.dimskiy.waypoints.domain.providers

import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.domain.model.DeviceLocation
import kotlinx.coroutines.flow.Flow

interface LocationsProvider {

    fun observeDeviceLocations(): Flow<DataResult<DeviceLocation>>
}