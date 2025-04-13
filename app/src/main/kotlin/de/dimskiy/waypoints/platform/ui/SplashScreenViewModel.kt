package de.dimskiy.waypoints.platform.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.dimskiy.waypoints.domain.waypointsrepository.WaypointsRepository
import de.dimskiy.waypoints.platform.di.BaseModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private const val UI_REDRAW_DELAY_SEC = 0.2

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    waypointsRepository: WaypointsRepository,
    @BaseModule.DispatcherDefault private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    val readyState: Flow<Unit> = waypointsRepository.observeWaypointsStored().map {
        delay(UI_REDRAW_DELAY_SEC.seconds)
    }.flowOn(coroutineDispatcher)
}