package de.dimskiy.waypoints.platform.errordisplay

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class MutableSharedHoldingFlow<T> : MutableSharedFlow<T> {

    private val wrappingFlow = MutableSharedFlow<T>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val cachedItem = AtomicReference<T?>(null)

    override suspend fun emit(value: T) {
        if (wrappingFlow.subscriptionCount.value > 0) {
            wrappingFlow.emit(value)
        } else {
            cachedItem.set(value)
        }
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        cachedItem.set(null)
        wrappingFlow.resetReplayCache()
    }

    override fun tryEmit(value: T): Boolean = if (wrappingFlow.subscriptionCount.value > 0) {
        wrappingFlow.tryEmit(value)
    } else {
        cachedItem.set(value)
        true
    }

    override val subscriptionCount: StateFlow<Int> = wrappingFlow.subscriptionCount

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        val lastItem = cachedItem.getAndSet(null)
        if (lastItem != null) {
            collector.emit(lastItem)
        }

        return wrappingFlow.collect(collector)
    }

    override val replayCache: List<T> = wrappingFlow.replayCache
}