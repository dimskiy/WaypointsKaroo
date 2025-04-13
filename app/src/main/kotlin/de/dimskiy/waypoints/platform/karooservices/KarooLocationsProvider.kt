package de.dimskiy.waypoints.platform.karooservices

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.model.DomainError
import de.dimskiy.waypoints.domain.providers.LocationsProvider
import de.dimskiy.waypoints.platform.di.BaseModule
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.OnLocationChanged
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

private const val GPS_SEARCH_TIMEOUT_SEC = 10

class KarooLocationsProvider @Inject constructor(
    private val karooServiceProvider: KarooServiceProvider,
    @ApplicationContext private val context: Context,
    @BaseModule.DispatcherDefault private val coroutineDispatcher: CoroutineDispatcher,
    @BaseModule.TimeProvider private val currentTimeProvider: Provider<Long>
) : LocationsProvider {

    private val lastLocationCache = MutableStateFlow<DataResult<DeviceLocation>>(
        DataResult.loading(context.getString(R.string.message_finding_the_location))
    )

    override fun observeDeviceLocations(): Flow<DataResult<DeviceLocation>> = channelFlow {
        withContext(coroutineDispatcher) {
            withTimeout(GPS_SEARCH_TIMEOUT_SEC.seconds) {
                karooServiceProvider.ensureConnected { service ->
                    send(getLocation(service))
                }
            }
        }
        awaitCancellation()
    }.onStart {
        emit(DataResult.loading(context.getString(R.string.message_finding_the_location)))
    }

    private suspend fun getLocation(service: KarooSystemService): DataResult<DeviceLocation> = try {
        Timber.d("Starting device location discovery")

        val locationResult = getCurrentLocationResult(service).mapResult {
            DeviceLocation(
                latitude = it.lat,
                longitude = it.lng,
                timestamp = currentTimeProvider.get()
            )
        }
        lastLocationCache.emit(locationResult)
        Timber.d("Location cache update: $locationResult")
        locationResult

    } catch (e: TimeoutCancellationException) {
        Timber.d("Device location discovery TIMEOUT")
        getCachedResultOrError()
    }

    private suspend fun getCurrentLocationResult(service: KarooSystemService): DataResult<OnLocationChanged> {
        var consumerId: String? = null

        val locationResult = suspendCancellableCoroutine { continuation ->
            consumerId = service.addConsumer<OnLocationChanged>(
                onError = {
                    continuation.resume(
                        DataResult.error<OnLocationChanged>(
                            DomainError.LocationServiceError(message = it)
                        )
                    )
                },
                onEvent = { location ->
                    Timber.d("Location discovered: $location")
                    continuation.resume(DataResult.ready(location))
                }
            )
        }

        consumerId?.let {
            service.removeConsumer(it)
            Timber.d("Location consumer ($it) removed")
        }

        return locationResult
    }

    private fun getCachedResultOrError(): DataResult<DeviceLocation> = lastLocationCache.value
        .takeIf(DataResult<*>::isReady)
        ?.let { cachedLocation ->
            Timber.d("Using cached location: $cachedLocation")
            cachedLocation
        }
        ?: DataResult.error(
            DomainError.LocationServiceError(
                message = context.getString(R.string.error_msg_no_location)
            )
        )
}