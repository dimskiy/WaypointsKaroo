package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.domain.cases.ObserveSearchResultsCase.Companion.LAST_LOCATION_OBSOLETE_TIMEOUT
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.model.DomainError
import de.dimskiy.waypoints.domain.model.ReportingModel
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.providers.LocationsProvider
import de.dimskiy.waypoints.domain.providers.ReportingProvider
import de.dimskiy.waypoints.domain.providers.SettingsProvider
import de.dimskiy.waypoints.domain.providers.WaypointsSearchProvider
import de.dimskiy.waypoints.domain.waypointsrepository.WaypointsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import javax.inject.Provider
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSearchResultsCaseTest {

    private lateinit var observeSearchResultsCase: ObserveSearchResultsCase
    private val testDispatcher = StandardTestDispatcher()
    private val currentTimestampMs = 1200.hours.inWholeMilliseconds
    private val languageLocaleEn = "EN"

    private val searchProviderMock = mockk<WaypointsSearchProvider>(relaxed = true)
    private val repositoryMock = mockk<WaypointsRepository>(relaxed = true)
    private val locationProviderMock = mockk<LocationsProvider>(relaxed = true)
    private val settingsProviderMock = mockk<SettingsProvider>(relaxed = true)
    private val reportingProviderMock = mockk<ReportingProvider>(relaxed = true)
    private val currentTimeProviderMock = mockk<Provider<Long>>(relaxed = true)

    @Before
    fun setUp() {
        every { currentTimeProviderMock.get() } returns currentTimestampMs

        observeSearchResultsCase = ObserveSearchResultsCase(
            searchProviderMock,
            repositoryMock,
            locationProviderMock,
            settingsProviderMock,
            reportingProviderMock,
            currentTimeProviderMock
        )
    }

    @Test
    fun `search results WHEN geoSearchDisabled`() = runTest(testDispatcher) {
        val query = "test"
        val expectedWaypoints = listOf(
            Waypoint.Discovered(
                serverId = "1",
                name = "test",
                latitude = 1.0,
                longitude = 2.0,
                address = mockk()
            )
        )
        every { settingsProviderMock.observeGeoSearchEnabled() } returns flowOf(false)
        coEvery {
            searchProviderMock.searchWaypoints(query, languageLocaleEn)
        } returns DataResult.ready(expectedWaypoints)

        // Check
        assertDataResultEquals(
            query = query,
            expected = DataResult.ready(expectedWaypoints)
        )

        verify {
            reportingProviderMock.report(ReportingModel.PerformSearch(false))
        }
    }

    @Test
    fun `search results WHEN geoSearchEnabled AND lastLocation outdated`() =
        runTest(testDispatcher) {
            val query = "test"
            val expectedWaypoints = listOf(
                Waypoint.Discovered(
                    serverId = "1",
                    name = "test",
                    latitude = 1.0,
                    longitude = 2.0,
                    address = mockk()
                )
            )
            val expectedLocation = DeviceLocation(1.0, 2.0, currentTimestampMs)
            val lastLocation = DeviceLocation(
                latitude = 1.0,
                longitude = 3.0,
                timestamp = currentTimestampMs - LAST_LOCATION_OBSOLETE_TIMEOUT.inWholeMilliseconds
            )

            every { settingsProviderMock.observeGeoSearchEnabled() } returns flowOf(true)
            every { settingsProviderMock.observeLastLocation() } returns flowOf(lastLocation)
            every {
                locationProviderMock.observeDeviceLocations()
            } returns flowOf(DataResult.ready(expectedLocation))

            coEvery {
                searchProviderMock.searchWaypointsWithLocation(
                    query,
                    languageLocaleEn,
                    expectedLocation
                )
            } returns DataResult.ready(expectedWaypoints)

            // Check
            assertDataResultEquals(
                query = query,
                expected = DataResult.ready(expectedWaypoints)
            )

            verify {
                reportingProviderMock.report(ReportingModel.PerformSearch(true))
            }

            coVerify {
                settingsProviderMock.saveLocation(expectedLocation)
            }
        }

    @Test
    fun `search results WHEN geoSearchEnabled AND lastLocation not obsolete`() =
        runTest(testDispatcher) {
            val query = "test"
            val expectedWaypoints = listOf(
                Waypoint.Discovered(
                    serverId = "1",
                    name = "test",
                    latitude = 1.0,
                    longitude = 2.0,
                    address = mockk()
                )
            )
            val expectedLocation = DeviceLocation(1.0, 2.0, currentTimestampMs)
            every { settingsProviderMock.observeGeoSearchEnabled() } returns flowOf(true)
            every { settingsProviderMock.observeLastLocation() } returns flowOf(expectedLocation)
            coEvery {
                searchProviderMock.searchWaypointsWithLocation(
                    query,
                    languageLocaleEn,
                    expectedLocation
                )
            } returns DataResult.ready(expectedWaypoints)

            // Check
            assertDataResultEquals(
                query = query,
                expected = DataResult.ready(expectedWaypoints)
            )

            verify {
                reportingProviderMock.report(ReportingModel.PerformSearch(true))
            }
        }

    @Test
    fun `location error WHEN geoSearchEnabled AND lastLocation obsolete AND locationProvider error`() =
        runTest(testDispatcher) {
            val query = "test"
            val expectedError = DomainError.LocationServiceError()

            every { settingsProviderMock.observeGeoSearchEnabled() } returns flowOf(true)
            every { settingsProviderMock.observeLastLocation() } returns flowOf(null)
            every {
                locationProviderMock.observeDeviceLocations()
            } returns flowOf(DataResult.error(expectedError))

            // Check
            assertDataResultEquals(
                query = query,
                expectBeginWithLoading = false,
                expected = DataResult.error<List<Waypoint>>(expectedError)
            )

            coVerify {
                locationProviderMock.observeDeviceLocations()
            }

            verify {
                reportingProviderMock.report(ReportingModel.PerformSearch(true))
            }
        }

    @Test
    fun `search error WHEN geoSearchEnabled AND lastLocation not obsolete AND searchProvider error`() =
        runTest(testDispatcher) {
            val query = "test"
            val expectedError = DomainError.NetworkError()
            val expectedLocation = DeviceLocation(1.0, 2.0, currentTimestampMs)

            every { settingsProviderMock.observeGeoSearchEnabled() } returns flowOf(true)
            every { settingsProviderMock.observeLastLocation() } returns flowOf(expectedLocation)
            coEvery {
                searchProviderMock.searchWaypointsWithLocation(
                    query,
                    languageLocaleEn,
                    expectedLocation
                )
            } returns DataResult.error(expectedError)

            // Check
            assertDataResultEquals(
                query = query,
                expected = DataResult.error<List<Waypoint>>(expectedError)
            )

            verify {
                reportingProviderMock.report(ReportingModel.PerformSearch(true))
            }
        }

    @Test
    fun `search error WHEN geoSearchDisabled AND searchProvider error`() =
        runTest(testDispatcher) {
            val query = "test"
            val expectedError = DomainError.NetworkError()

            every { settingsProviderMock.observeGeoSearchEnabled() } returns flowOf(false)
            coEvery {
                searchProviderMock.searchWaypoints(query, languageLocaleEn)
            } returns DataResult.error(expectedError)

            // Check
            assertDataResultEquals(
                query = query,
                expected = DataResult.error<List<Waypoint>>(expectedError)
            )
        }

    private suspend fun <T> assertDataResultEquals(
        query: String,
        expectBeginWithLoading: Boolean = true,
        expected: DataResult<T>
    ) {
        every { repositoryMock.observeWaypointsStored() } returns flowOf(emptyList())

        assertEquals(
            listOfNotNull(
                if (expectBeginWithLoading) DataResult.loading() else null,
                expected
            ),
            observeSearchResultsCase.invoke(query, languageLocaleEn)
                .take(if (expectBeginWithLoading) 2 else 1)
                .toList()
        )
    }
}