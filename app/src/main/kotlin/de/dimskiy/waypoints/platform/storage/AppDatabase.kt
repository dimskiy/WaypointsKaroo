package de.dimskiy.waypoints.platform.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import de.dimskiy.waypoints.platform.storage.entity.WaypointEntity

@Database(
    entities = [WaypointEntity::class],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun waypointDao(): WaypointsDao

    companion object {
        const val DB_NAME = "app_database"
    }
}