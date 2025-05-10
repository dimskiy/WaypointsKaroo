package de.dimskiy.waypoints.platform.karooservices

import android.content.Context
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.model.DomainError
import io.hammerhead.karooext.models.OnLocationChanged
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import javax.inject.Provider
import kotlin.time.Duration.Companion.seconds

package de.dimskiy.waypoints.platform.karooservices

import android.content.Context
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.model.DomainError
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.OnLocationChanged
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class KarooLocationsProviderTest {

    private val karooServiceProvider: KarooServiceProvider = mockk()
    private val mockContext: Context = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private val timeProvider: Provider<Long> = mockk()

    private lateinit var karooLocationsProvider: KarooLocationsProvider

    private val locationFlow = MutableSharedFlow<OnLocationChanged>()

    @Before
    fun setUp() {
        every { mockContext.getString(R.string.message_finding_the_location) } returns "Finding location..."
        every { mockContext.getString(R.string.error_msg_no_location) } returns "No location available."
        every { timeProvider.get() } returns 1678886400000L // Arbitrary timestamp

        every { karooServiceProvider.observeEvent<OnLocationChanged>(OnLocationChanged.Params) } returns locationFlow

        karooLocationsProvider = KarooLocationsProvider(
            karooServiceProvider,
            mockContext,
            testDispatcher,
            timeProvider
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
        val testLocation = OnLocationChanged(lat = 1.0, lng = 2.0, accuracy = 3.0, altitude = 4.0, speed = 5.0, timestamp = 0L, bearing = 6.0)
        val expectedLocation = DeviceLocation(latitude = 1.0, longitude = 2.0, timestamp = 1678886400000L)

        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading
            locationFlow.emit(testLocation)
            assertEquals(DataResult.ready(expectedLocation), awaitItem())
        }
    }

    @Test
    fun `error result WHEN timeout occurs AND no cached location`() = runTest(testDispatcher) {
        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading

            testDispatcher.scheduler.advanceTimeBy(11.seconds) // Advance beyond timeout

            assertEquals(DataResult.error(DomainError.LocationServiceError(message = "No location available.")), awaitItem())
        }
    }

    @Test
    fun `ready result from cache WHEN timeout occurs AND cached location available`() = runTest(testDispatcher) {
        val initialLocation = OnLocationChanged(lat = 10.0, lng = 20.0, accuracy = 30.0, altitude = 40.0, speed = 50.0, timestamp = 0L, bearing = 60.0)
        val cachedDeviceLocation = DeviceLocation(latitude = 10.0, longitude = 20.0, timestamp = 1678886400000L)

        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading
            locationFlow.emit(initialLocation)
            assertEquals(DataResult.ready(cachedDeviceLocation), awaitItem())

            testDispatcher.scheduler.advanceTimeBy(11.seconds) // Timeout occurs

            assertEquals(DataResult.ready(cachedDeviceLocation), awaitItem())
        }
    }

    @Test
    fun `error result WHEN underlying flow throws error`() = runTest(testDispatcher) {
        val testError = RuntimeException("Service error")
        every { karooServiceProvider.observeEvent<OnLocationChanged>(OnLocationChanged.Params) } returns locationFlow.catch { throw testError }

        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading
            assertEquals(DataResult.error(DomainError.LocationServiceError(testError)), awaitItem())
        }
    }

    @Test
    fun `multiple ready results WHEN multiple OnLocationChanged emitted`() = runTest(testDispatcher) {
        val location1 = OnLocationChanged(lat = 1.0, lng = 2.0, accuracy = 3.0, altitude = 4.0, speed = 5.0, timestamp = 0L, bearing = 6.0)
        val location2 = OnLocationChanged(lat = 10.0, lng = 20.0, accuracy = 30.0, altitude = 40.0, speed = 50.0, timestamp = 0L, bearing = 60.0)

        val deviceLocation1 = DeviceLocation(latitude = 1.0, longitude = 2.0, timestamp = 1678886400000L)
        val deviceLocation2 = DeviceLocation(latitude = 10.0, longitude = 20.0, timestamp = 1678886400000L)

        karooLocationsProvider.observeDeviceLocations().test {
            awaitItem() // Skip initial loading
            locationFlow.emit(location1)
            assertEquals(DataResult.ready(deviceLocation1), awaitItem())

            locationFlow.emit(location2)
            assertEquals(DataResult.ready(deviceLocation2), awaitItem())
        }
    }
}