package de.dimskiy.waypoints.platform.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.dimskiy.waypoints.platform.storage.entity.WaypointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(waypoint: WaypointEntity)

    @Query("SELECT * FROM ${WaypointEntity.TABLE_NAME} WHERE ${WaypointEntity.COLUMN_ID} = :id LIMIT 1")
    suspend fun selectById(id: Int): WaypointEntity?

    @Query(
        """SELECT * FROM ${WaypointEntity.TABLE_NAME}
                WHERE ${WaypointEntity.COLUMN_SERVER_ID} = :serverId 
                AND ${WaypointEntity.COLUMN_SERVER_TYPE} = :serverType 
            LIMIT 1"""
    )
    suspend fun selectByServerId(
        serverId: String,
        serverType: WaypointEntity.ServerType = WaypointEntity.ServerType.PHOTON
    ): WaypointEntity?

    @Query("SELECT * FROM ${WaypointEntity.TABLE_NAME} ORDER BY ${WaypointEntity.COLUMN_NAME} ASC")
    fun selectAll(): Flow<List<WaypointEntity>>

    @Query("DELETE FROM ${WaypointEntity.TABLE_NAME} WHERE ${WaypointEntity.COLUMN_ID} = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM waypoints")
    suspend fun deleteAll()
}