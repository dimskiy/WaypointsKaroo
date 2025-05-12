package de.dimskiy.waypoints.platform.karooservices

import de.dimskiy.waypoints.model.LocalException
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.KarooEvent
import io.hammerhead.karooext.models.KarooEventParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import kotlin.coroutines.resumeWithException

class KarooServiceProvider(
    private val karooService: KarooSystemService
) {
    @Volatile
    private var activeConsumers = 0
    private val mutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class)
    inline fun <reified T : KarooEvent> observeEvent(eventParams: KarooEventParams): Flow<T> =
        channelFlow {
            ensureConnected { karooService ->
                val result = suspendCancellableCoroutine<T> { continuation ->
                    var consumerId: String? = null
                    consumerId = karooService.addConsumer<T>(
                        params = eventParams,
                        onError = {
                            Timber.d("Request error: $it")
                            consumerId?.let(karooService::removeConsumer)
                            continuation.resumeWithException(IllegalStateException(it))
                        },
                        onEvent = { response ->
                            continuation.resume(response) {
                                Timber.d("Coroutine cancelled for receiving event ($eventParams)")
                            }
                        },
                        onComplete = {
                            Timber.d("Http consumer onComplete")
                            consumerId?.let(karooService::removeConsumer)
                        }
                    )
                }

                send(result)
            }
        }

    suspend fun ensureConnected(block: suspend (KarooSystemService) -> Unit) {
        Timber.d("Service call requested")

        try {
            mutex.withLock { activeConsumers++ }

            if (karooService.connected) {
                Timber.d("Service connected already")
                block(karooService)
            } else {
                awaitServiceConnection()
                Timber.d("Service CONNECTED")
                block(karooService)
            }
        } finally {
            mutex.withLock { activeConsumers-- }
            Timber.d("Active consumers count: $activeConsumers")

            if (activeConsumers == 0 && karooService.connected) {
                karooService.disconnect()
                Timber.d("No active consumers - service DISCONNECTED")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun awaitServiceConnection() = suspendCancellableCoroutine { continuation ->
        if (karooService.libVersion.isNullOrEmpty()) {
            Timber.e("Karoo service not found - is running on Karoo device?")
            continuation.resumeWithException(LocalException.KarooServiceException)
        }

        karooService.connect { isConnected ->
            when {
                isConnected && continuation.isActive -> {
                    continuation.resume(Unit) {
                        Timber.d(it, "Service preparation cancelled")
                    }
                }

                !isConnected && continuation.isActive -> {
                    Timber.e("Cannot connect to Karoo service while the service apparently present")
                    continuation.resumeWithException(LocalException.KarooServiceException)
                }

                else -> Timber.d("Attempt to resume completed coroutine, skipping")
            }
        }
    }
}