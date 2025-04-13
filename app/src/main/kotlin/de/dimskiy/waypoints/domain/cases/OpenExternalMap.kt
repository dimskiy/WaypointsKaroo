package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.platform.di.BaseModule
import de.dimskiy.waypoints.platform.karooservices.KarooServiceProvider
import io.hammerhead.karooext.models.LaunchPinDrop
import io.hammerhead.karooext.models.Symbol
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OpenExternalMap @Inject constructor(
    private val karooProvider: KarooServiceProvider,
    @BaseModule.DispatcherDefault private val coroutineDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(model: Waypoint) = withContext(coroutineDispatcher) {
        karooProvider.ensureConnected {
            it.dispatch(
                LaunchPinDrop(
                    Symbol.POI(
                        id = "poi${model.latitude}-${model.longitude}",
                        lat = model.latitude,
                        lng = model.longitude
                    )
                )
            )
        }
    }
}