package de.dimskiy.waypoints.domain.waypointsrepository

import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.platform.storage.WaypointsDao
import de.dimskiy.waypoints.platform.storage.entity.WaypointEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class WaypointsRepositoryImpl @Inject constructor(
    private val storage: WaypointsDao
) : WaypointsRepository {

    override fun observeWaypointsStored(): Flow<List<Waypoint.Stored>> {
        return storage.selectAll().map { entities ->
            entities.map(::mapToWaypoint)
        }.onEach {
            Timber.d("Stored waypoints emit: $it")
        }
    }

    override suspend fun getWaypointStored(serverId: String): Waypoint.Stored? {
        val item = storage.selectByServerId(serverId)?.let(::mapToWaypoint)?.also {
            Timber.d("Waypoint found in storage: serverId($serverId) == $it ")
        }

        return item
    }

    override suspend fun setWaypointStored(waypoint: Waypoint) {
        val entity = mapToEntity(waypoint)
        storage.insert(entity)

        Timber.d("Waypoint stored: $entity")
    }

    override suspend fun deleteWaypointStored(waypoint: Waypoint.Stored) {
        Timber.d("Waypoint deletion requested: $waypoint")

        waypoint.id.let {
            storage.delete(it)
            Timber.d("Waypoint deleted: ID == $it")
        }
    }

    private fun mapToWaypoint(entity: WaypointEntity) = Waypoint.Stored(
        id = entity.id,
        serverId = entity.serverId,
        name = entity.name,
        address = Waypoint.Address(
            country = entity.country,
            city = entity.city,
            zip = entity.zip,
            street = entity.street,
            qualifier1 = entity.qualifier1,
            qualifier2 = entity.qualifier2,
        ),
        latitude = entity.latitude,
        longitude = entity.longitude
    )

    private fun mapToEntity(waypoint: Waypoint) = WaypointEntity(
        serverId = waypoint.serverId,
        name = waypoint.name,
        country = waypoint.address.country,
        city = waypoint.address.city,
        zip = waypoint.address.zip,
        qualifier1 = waypoint.address.qualifier1,
        qualifier2 = waypoint.address.qualifier2,
        street = waypoint.address.street,
        latitude = waypoint.latitude,
        longitude = waypoint.longitude,
    )
}