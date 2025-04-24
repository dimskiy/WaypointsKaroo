package de.dimskiy.waypoints.platform.ui.screens.waypointslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.domain.cases.GetDistanceToDeviceLocationCase
import de.dimskiy.waypoints.domain.cases.ObserveSearchResultsCase
import de.dimskiy.waypoints.domain.cases.ObserveWaypointsStoredCase
import de.dimskiy.waypoints.domain.cases.OpenExternalMap
import de.dimskiy.waypoints.domain.cases.ToggleWaypointBookmarkCase
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.domain.providers.SettingsProvider
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.SearchResponse
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.UserIntent
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.WaypointWithDistance
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.WaypointsListState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WaypointsListViewModel @Inject constructor(
    private val observeSearchResults: ObserveSearchResultsCase,
    private val toggleWaypointBookmark: ToggleWaypointBookmarkCase,
    private val getDistanceToDeviceLocation: GetDistanceToDeviceLocationCase,
    private val openExternalMap: OpenExternalMap,
    private val settingsProvider: SettingsProvider,
    observeWaypointsStored: ObserveWaypointsStoredCase
) : ViewModel() {

    private val searchResults = MutableStateFlow<DataResult<List<Waypoint>>>(
        DataResult.ready(emptyList())
    )

    private var waypointsSearchJob: Job = Job()
        set(value) {
            field.cancel()
            field = value
        }

    val viewState: StateFlow<WaypointsListState> = combine(
        observeWaypointsStored(),
        searchResults,
        settingsProvider.observeGeoSearchEnabled()
    ) { bookmarks, searchResult, geoSearchEnabled ->
        WaypointsListState(
            bookmarks = bookmarks,
            searchResponse = SearchResponse(
                dataResult = searchResult.mapResultSuspend { getUpdatedWithDistance(it) },
                providerName = observeSearchResults.providerName,
                geoSearchEnabled = geoSearchEnabled
            )
        )
    }.onEach {
        Timber.d("ViewState emission: $it")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = WaypointsListState(
            bookmarks = emptyList(),
            searchResponse = SearchResponse(
                dataResult = DataResult.ready(emptyList()),
                providerName = observeSearchResults.providerName,
                geoSearchEnabled = false
            )
        )
    )

    private suspend fun getUpdatedWithDistance(waypoints: List<Waypoint>): List<WaypointWithDistance> {
        val isGeoSearchEnabled = settingsProvider.observeGeoSearchEnabled().first()

        return if (isGeoSearchEnabled) {
            val waypointsWithDistances = getDistanceToDeviceLocation(waypoints)
            waypointsWithDistances.sortedBy { it.distanceToDeviceKm }
        } else {
            waypoints.map { waypoint ->
                WaypointWithDistance(null, waypoint)
            }.sortedBy { it.waypoint.name }
        }
    }

    fun onUserIntent(intent: UserIntent) {
        Timber.d("User intent received: $intent")

        when (intent) {
            is UserIntent.ClickWaypoint -> onWaypointClick(intent.model)
            is UserIntent.DismissWaypoint -> onWaypointDismiss(intent.model)
            is UserIntent.PerformSearch -> onSearchRequested(
                intent.query,
                intent.resultsLanguageCode
            )

            is UserIntent.ToggleBookmark -> onBookmarkToggle(intent.model)
            is UserIntent.ToggleGeoSearch -> onGeoSearchToggle(intent.isEnabled)
        }
    }

    private fun onSearchRequested(query: String, resultsLanguageCode: String) {
        if (query.isEmpty()) {
            waypointsSearchJob.cancel()
            searchResults.update { DataResult.ready(emptyList()) }
        } else {
            waypointsSearchJob = viewModelScope.launch {
                observeSearchResults(query, resultsLanguageCode).collectLatest(searchResults::emit)
            }
        }
    }

    private fun onBookmarkToggle(model: Waypoint) {
        viewModelScope.launch {
            toggleWaypointBookmark(model)
        }
    }

    private fun onWaypointDismiss(model: Waypoint) {
        viewModelScope.launch {
            toggleWaypointBookmark(model)
        }
    }

    private fun onWaypointClick(model: Waypoint) {
        viewModelScope.launch {
            openExternalMap(model)
        }
    }

    private fun onGeoSearchToggle(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsProvider.setGeoSearchEnabled(isEnabled)
        }
    }
}