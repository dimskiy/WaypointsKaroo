package de.dimskiy.waypoints.platform.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import de.dimskiy.waypoints.platform.storage.entity.WaypointEntity

@Database(entities = [WaypointEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun waypointDao(): WaypointsDao
}