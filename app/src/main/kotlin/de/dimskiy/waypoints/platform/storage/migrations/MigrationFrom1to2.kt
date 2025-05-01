package de.dimskiy.waypoints.platform.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.dimskiy.waypoints.platform.storage.AppDatabase
import de.dimskiy.waypoints.platform.storage.entity.WaypointEntity

object MigrationFrom1to2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val tableName = WaypointEntity.TABLE_NAME
        val tempTableName = "temp_${AppDatabase.DB_NAME}"

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $tempTableName 
                (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `serverType` TEXT NOT NULL, 
                    `serverId` TEXT NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `country` TEXT, 
                    `city` TEXT, 
                    `zip` TEXT, 
                    `street` TEXT, 
                    `qualifier1` TEXT, 
                    `qualifier2` TEXT, 
                    `latitude` REAL NOT NULL, 
                    `longitude` REAL NOT NULL
                )
        """.trimIndent()
        )
        database.execSQL("INSERT INTO $tempTableName SELECT * FROM $tableName")
        database.execSQL("DROP TABLE $tableName")
        database.execSQL("ALTER TABLE $tempTableName RENAME TO $tableName")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_${tableName}_${WaypointEntity.COLUMN_SERVER_ID} ON $tableName (${WaypointEntity.COLUMN_SERVER_ID})")
    }
}