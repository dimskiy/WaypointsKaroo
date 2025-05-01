package de.dimskiy.waypoints.platform.storage

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.dimskiy.waypoints.platform.storage.entity.WaypointEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class WaypointsDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var waypointsDao: WaypointsDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            context = InstrumentationRegistry.getInstrumentation().context,
            klass = AppDatabase::class.java
        ).build()

        waypointsDao = database.waypointDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_waypoint() = runTest {
        val waypoint = createWaypoint()

        waypointsDao.insert(waypoint)

        assertEquals(
            waypoint,
            waypointsDao.selectById(waypoint.id)
        )
    }

    @Test
    fun replace_WHEN_insertMultipleWaypoints_AND_sameServerId() = runTest {
        val waypoint1 = createWaypoint(id = 1, serverId = "11")
        val waypoint2 = createWaypoint(id = 2, serverId = "12")
        val waypoint2NewId = waypoint2.copy(id = 3)

        waypointsDao.insert(waypoint1)
        waypointsDao.insert(waypoint2)
        waypointsDao.insert(waypoint2NewId)

        assertEquals(
            listOf(
                waypoint1,
                waypoint2NewId
            ),
            waypointsDao.selectAll().first()
        )
    }

    @Test
    fun update_waypoint() = runTest {
        val initialWaypoint = createWaypoint()
        val updatedWaypoint = initialWaypoint.copy(name = "Updated Waypoint")

        waypointsDao.insert(initialWaypoint)
        waypointsDao.insert(updatedWaypoint)

        assertEquals(
            updatedWaypoint,
            waypointsDao.selectById(initialWaypoint.id)
        )
    }

    @Test
    fun selectAll() = runTest {
        val waypoints = listOf(
            createWaypoint(id = 1, serverId = "11"),
            createWaypoint(id = 2, serverId = "12"),
            createWaypoint(id = 3, serverId = "13"),
        )

        waypoints.onEach {
            waypointsDao.insert(it)
        }

        assertEquals(
            waypoints,
            waypointsDao.selectAll().first()
        )
    }

    @Test
    fun selectById_WHEN_noMatch() = runTest {
        val retrievedWaypoint = waypointsDao.selectById(11)

        assertNull(retrievedWaypoint)
    }

    @Test
    fun selectByServerId() = runTest {
        val waypoint = createWaypoint()

        waypointsDao.insert(waypoint)

        assertEquals(
            waypoint,
            waypointsDao.selectByServerId(waypoint.serverId)
        )
    }

    @Test
    fun selectByServerId_WHEN_noMatch() = runTest {
        val retrievedWaypoint = waypointsDao.selectByServerId("someId")

        assertNull(retrievedWaypoint)
    }

    @Test
    fun deleteItem() = runTest {
        val waypoint1 = createWaypoint(id = 1, serverId = "11")
        val waypoint2 = createWaypoint(id = 2, serverId = "12")

        waypointsDao.insert(waypoint1)
        waypointsDao.insert(waypoint2)
        waypointsDao.delete(waypoint1.id)

        assertEquals(
            listOf(waypoint2),
            waypointsDao.selectAll().first()
        )
    }

    @Test
    fun noException_WHEN_deleteItem_AND_noMatch() = runTest {
        waypointsDao.delete(11)
    }

    @Test
    fun deleteAll() = runTest {
        val waypoint1 = createWaypoint(id = 1, serverId = "11")
        val waypoint2 = createWaypoint(id = 2, serverId = "12")

        waypointsDao.insert(waypoint1)
        waypointsDao.insert(waypoint2)
        waypointsDao.deleteAll()

        assertEquals(
            emptyList<WaypointEntity>(),
            waypointsDao.selectAll().first()
        )
    }

    private fun createWaypoint(id: Int = 1, serverId: String = "testId") = WaypointEntity(
        id = id,
        serverId = serverId,
        name = "Test Waypoint",
        latitude = 1.0,
        longitude = 2.0,
        serverType = WaypointEntity.ServerType.PHOTON,
        country = "Germany",
        city = "Berlin",
        zip = "123456",
        street = "Alexanderplatz",
        qualifier1 = "qualifier1",
        qualifier2 = "qualifier2",
    )
}