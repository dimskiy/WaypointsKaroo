package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.providers.EnvironmentPropertiesProvider
import de.dimskiy.waypoints.domain.providers.SettingsProvider
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.WaypointWithDistance
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GetDistanceToDeviceLocationCaseTest {

    private lateinit var getDistanceCase: GetDistanceToDeviceLocationCase

    private val settingsProviderMock = mockk<SettingsProvider>()
    private val propertiesProviderMock = mockk<EnvironmentPropertiesProvider>()
    private val testDispatcher: CoroutineDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        getDistanceCase = GetDistanceToDeviceLocationCase(
            settingsProvider = settingsProviderMock,
            propertiesProvider = propertiesProviderMock,
            coroutineDispatcher = testDispatcher
        )
    }

    @Test
    fun `get distance WHEN last location AND units metric`() = runTest(testDispatcher) {
        coEvery { propertiesProviderMock.isMeasureUnitMetric() } returns true
        coEvery { settingsProviderMock.observeLastLocation() } returns flowOf(
            DeviceLocation(11.11, 12.12, 11111111L)
        )
        val waypoint = getWaypointMock(11.12, 12.12)

        Assert.assertEquals(
            listOf(
                WaypointWithDistance(
                    distanceToDeviceKm = 1.1119492664455637,
                    isDistanceMetric = true,
                    waypoint = waypoint
                )
            ),
            getDistanceCase(listOf(waypoint))
        )
    }

    @Test
    fun `get distance WHEN last location AND units imperial`() = runTest(testDispatcher) {
        coEvery { propertiesProviderMock.isMeasureUnitMetric() } returns false
        coEvery { settingsProviderMock.observeLastLocation() } returns flowOf(
            DeviceLocation(11.11, 12.12, 11111111L)
        )
        val waypoint = getWaypointMock(11.12, 12.12)

        Assert.assertEquals(
            listOf(
                WaypointWithDistance(
                    distanceToDeviceKm = 1.1119492664455637,
                    isDistanceMetric = false,
                    waypoint = waypoint
                )
            ),
            getDistanceCase(listOf(waypoint))
        )
    }

    @Test
    fun `no distance WHEN no last location AND units metric`() = runTest(testDispatcher) {
        coEvery { propertiesProviderMock.isMeasureUnitMetric() } returns true
        coEvery { settingsProviderMock.observeLastLocation() } returns flowOf(null)
        val waypoint = getWaypointMock(11.12, 12.12)

        Assert.assertEquals(
            listOf(
                WaypointWithDistance(
                    distanceToDeviceKm = null,
                    isDistanceMetric = true,
                    waypoint = waypoint
                )
            ),
            getDistanceCase(listOf(waypoint))
        )
    }

    @Test
    fun `no distance WHEN no last location AND units imperial`() = runTest(testDispatcher) {
        coEvery { propertiesProviderMock.isMeasureUnitMetric() } returns false
        coEvery { settingsProviderMock.observeLastLocation() } returns flowOf(null)
        val waypoint = getWaypointMock(11.12, 12.12)

        Assert.assertEquals(
            listOf(
                WaypointWithDistance(
                    distanceToDeviceKm = null,
                    isDistanceMetric = false,
                    waypoint = waypoint
                )
            ),
            getDistanceCase(listOf(waypoint))
        )
    }

    private fun getWaypointMock(latitude: Double, longitude: Double) = mockk<Waypoint>().apply {
        every { this@apply.latitude } returns latitude
        every { this@apply.longitude } returns longitude
    }
}