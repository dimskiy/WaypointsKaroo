package de.dimskiy.waypoints.platform.karooservices

import android.content.Context
import app.cash.turbine.test
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.model.LocalError
import io.hammerhead.karooext.models.OnLocationChanged
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import javax.inject.Provider
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class KarooLocationsProviderTest {

    private val currentTimestamp = 120000000L
    private val testDispatcher = StandardTestDispatcher()

    private val serviceProviderMock: KarooServiceProvider = mockk(relaxed = true)
    private val contextMock: Context = mockk(relaxed = true)
    private val timeProviderMock: Provider<Long> = mockk(relaxed = true)

    private lateinit var karooLocationsProvider: KarooLocationsProvider

    @Before
    fun setUp() {
        every { contextMock.getString(R.string.message_finding_the_location) } returns "Finding location..."
        every { contextMock.getString(R.string.error_msg_no_location) } returns "No location available."
        every { timeProviderMock.get() } returns currentTimestamp

        karooLocationsProvider = KarooLocationsProvider(
            serviceProviderMock,
            contextMock,
            testDispatcher,
            timeProviderMock
        )
    }

    @Test
    fun `loading result WHEN observeDeviceLocations called AND initially emits loading`() = runTest(testDispatcher) {
        karooLocationsProvider.observeDeviceLocations().test {
            assertEquals(DataResult.loading("Finding location..."), awaitItem())
        }
    }

    @Test
    fun `ready result WHEN OnLocationChanged emitted`() = runTest(testDispatcher) {
        val testLocation = OnLocationChanged(lat = 1.0, lng = 2.0, orientation = 3.0)
        val expectedLocation = DeviceLocation(latitude = 1.0, longitude = 2.0, timestamp = 1678886400000L)
        coEvery { serviceProviderMock.observeEvent<OnLocationChanged>(OnLocationChanged.Params) } returns flowOf(testLocation)

        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem()
            assertEquals(DataResult.ready(expectedLocation), awaitItem())
        }
    }

    @Test
    fun `error result WHEN timeout occurs AND no cached location`() = runTest(testDispatcher) {
        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading

            testDispatcher.scheduler.advanceTimeBy(11.seconds) // Advance beyond timeout

            assertEquals(DataResult.error<DeviceLocation>(LocalError.LocationServiceError(message = "No location available.")), awaitItem())
        }
    }

    @Test
    fun `ready result from cache WHEN timeout occurs AND cached location available`() = runTest(testDispatcher) {
        val initialLocation = DeviceLocation(10.0, 20.0, currentTimestamp)
        val cachedDeviceLocation = DeviceLocation(latitude = 10.0, longitude = 20.0, timestamp = 1678886400000L)
        every { karooLocationsProvider.observeDeviceLocations() } returns flowOf(DataResult.ready(initialLocation))

        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading
            assertEquals(DataResult.ready(cachedDeviceLocation), awaitItem())

            testDispatcher.scheduler.advanceTimeBy(11.seconds) // Timeout occurs

            assertEquals(DataResult.ready(cachedDeviceLocation), awaitItem())
        }
    }

    @Test
    fun `error result WHEN underlying flow throws error`() = runTest(testDispatcher) {
        val testError = RuntimeException("Service error")
        every { serviceProviderMock.observeEvent<OnLocationChanged>(OnLocationChanged.Params) } returns flow { throw testError }

        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading
            assertEquals(DataResult.error<DeviceLocation>(LocalError.LocationServiceError(testError)), awaitItem())
        }
    }

    @Test
    fun `multiple ready results WHEN multiple OnLocationChanged emitted`() = runTest(testDispatcher) {
        val location1 = OnLocationChanged(lat = 1.0, lng = 2.0, orientation = 3.0)
        val location2 = OnLocationChanged(lat = 10.0, lng = 20.0, orientation = 3.0)

        val deviceLocation1 = DeviceLocation(latitude = 1.0, longitude = 2.0, timestamp = 1678886400000L)
        val deviceLocation2 = DeviceLocation(latitude = 10.0, longitude = 20.0, timestamp = 1678886400000L)

        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading
            assertEquals(DataResult.ready(deviceLocation1), awaitItem())

            assertEquals(DataResult.ready(deviceLocation2), awaitItem())
        }
    }
}