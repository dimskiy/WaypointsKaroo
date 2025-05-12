package de.dimskiy.waypoints.platform.karooservices

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.providers.LocationsProvider
import de.dimskiy.waypoints.model.LocalException
import de.dimskiy.waypoints.platform.di.BaseModule
import io.hammerhead.karooext.models.OnLocationChanged
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.Duration.Companion.seconds

private const val GPS_SEARCH_TIMEOUT_SEC = 10

class KarooLocationsProvider @Inject constructor(
    private val karooServiceProvider: KarooServiceProvider,
    @ApplicationContext private val context: Context,
    @BaseModule.DispatcherDefault private val coroutineDispatcher: CoroutineDispatcher,
    @BaseModule.TimeProvider private val currentTimeProvider: Provider<Long>
) : LocationsProvider {

    private val lastLocationCache = MutableStateFlow<DeviceLocation?>(null)

    @OptIn(FlowPreview::class)
    override fun observeDeviceLocations(): Flow<DataResult<DeviceLocation>> =
        karooServiceProvider.observeEvent<OnLocationChanged>(OnLocationChanged.Params)
            .flowOn(coroutineDispatcher)
            .timeout(GPS_SEARCH_TIMEOUT_SEC.seconds)
            .map<OnLocationChanged, DataResult<DeviceLocation>> { onLocationChangeResponse ->
                DataResult.ready(
                    DeviceLocation(
                        latitude = onLocationChangeResponse.lat,
                        longitude = onLocationChangeResponse.lng,
                        timestamp = currentTimeProvider.get()
                    )
                )
            }
            .onEach { locationResult -> lastLocationCache.update { locationResult.data } }
            .catch { error ->
                if (error is TimeoutCancellationException) {
                    Timber.d("Device location discovery TIMEOUT")
                    emit(getCachedResultOrError())
                } else {
                    emit(DataResult.error(LocalException.LocationServiceException(error)))
                }
            }
            .onStart { emit(DataResult.loading(context.getString(R.string.message_finding_the_location))) }

    private fun getCachedResultOrError(): DataResult<DeviceLocation> =
        lastLocationCache.value?.let {
            DataResult.ready(it)
        } ?: DataResult.error(
            LocalException.LocationServiceException(
                message = context.getString(R.string.error_msg_no_location)
            )
        )
}