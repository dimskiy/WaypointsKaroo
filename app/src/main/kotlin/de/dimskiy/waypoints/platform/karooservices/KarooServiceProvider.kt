package de.dimskiy.waypoints.platform.karooservices

import io.hammerhead.karooext.KarooSystemService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class KarooServiceProvider(
    private val karooService: KarooSystemService
) {
    @Volatile
    private var activeConsumers = 0
    private val mutex = Mutex()

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
        karooService.connect {
            continuation.resume(Unit) {
                Timber.d(it, "Service preparation cancelled")
            }
        }
    }
}