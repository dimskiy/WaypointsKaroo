package de.dimskiy.waypoints.platform.storage.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.dimskiy.waypoints.platform.storage.AppDatabase
import de.dimskiy.waypoints.platform.storage.entity.WaypointEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val testDbName = "test-database"

    @Test
    fun migrate1To2() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL(
                """
                    CREATE TABLE IF NOT EXISTS ${WaypointEntity.TABLE_NAME} 
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
            execSQL(
                """
                    INSERT INTO ${WaypointEntity.TABLE_NAME} 
                        (serverType, serverId, name, country, city, zip, street, qualifier1, qualifier2, latitude, longitude) 
                    VALUES 
                        ('PHOTON', 'srv_123', 'Test Waypoint', 'Germany', 'Berlin', '10115', 'Main Street', 'Qualifier A', 'Qualifier B', 52.5200, 13.4050)
                """.trimIndent()
            )
            close()
        }

        val dbUpdatedState = helper.runMigrationsAndValidate(testDbName, 2, true, MigrationFrom1to2)

        with(dbUpdatedState.query("SELECT * FROM ${WaypointEntity.TABLE_NAME}")) {
            assertTrue("Should contain one entry", moveToFirst())

            assertEquals("PHOTON", getString(getColumnIndexOrThrow("serverType")))
            assertEquals("srv_123", getString(getColumnIndexOrThrow("serverId")))
            assertEquals("Test Waypoint", getString(getColumnIndexOrThrow("name")))
            assertEquals("Germany", getString(getColumnIndexOrThrow("country")))
            assertEquals("Berlin", getString(getColumnIndexOrThrow("city")))
            assertEquals("10115", getString(getColumnIndexOrThrow("zip")))
            assertEquals("Main Street", getString(getColumnIndexOrThrow("street")))
            assertEquals(null, getString(getColumnIndexOrThrow("house")))
            assertEquals("Qualifier A", getString(getColumnIndexOrThrow("qualifier1")))
            assertEquals("Qualifier B", getString(getColumnIndexOrThrow("qualifier2")))
            assertEquals(52.5200, getDouble(getColumnIndexOrThrow("latitude")), 0.0)
            assertEquals(13.4050, getDouble(getColumnIndexOrThrow("longitude")), 0.0)
        }
    }
}