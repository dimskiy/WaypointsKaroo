package de.dimskiy.waypoints.domain.cases

import de.dimskiy.waypoints.domain.model.ReportingModel
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.providers.ReportingProvider
import de.dimskiy.waypoints.platform.di.BaseModule
import de.dimskiy.waypoints.platform.karooservices.KarooServiceProvider
import io.hammerhead.karooext.models.LaunchPinDrop
import io.hammerhead.karooext.models.Symbol
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OpenExternalMap @Inject constructor(
    private val karooProvider: KarooServiceProvider,
    private val reportingProvider: ReportingProvider,
    @BaseModule.DispatcherDefault private val coroutineDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(model: Waypoint) = withContext(coroutineDispatcher) {
        reportingProvider.report(ReportingModel.SearchResultClick)

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