package de.dimskiy.waypoints.platform.ui.screens.waypointslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.platform.ui.PreviewOnKaroo2
import de.dimskiy.waypoints.platform.ui.components.InfoContentWidget
import de.dimskiy.waypoints.platform.ui.components.NavigationOverlay
import de.dimskiy.waypoints.platform.ui.components.WaypointBookmarkItem
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.SearchResponse
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.UserIntent
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.WaypointsListState
import de.dimskiy.waypoints.platform.ui.theme.AppTheme

//region "Composable previews"
@PreviewOnKaroo2
@Composable
fun WaypointsListPreview() {
    AppTheme(darkTheme = true) {
        WaypointsListScreenContent(
            viewState = WaypointsListState(
                bookmarks = listOf(
                    Waypoint.Stored(
                        1,
                        "1",
                        "Name 1 longer than first",
                        Waypoint.Address("Country", "City", "12345", "Spuelmachinestr 12", "smth", "smwhr"),
                        12.0343302,
                        34.434566,
                    ),
                    Waypoint.Stored(
                        2,
                        "2",
                        "Name 2 longer than first",
                        Waypoint.Address("Country", "City", "12345", "Spuelmachinestr 12", "smth", "smwhr"),
                        12.0343302,
                        34.434566,
                    ),
                ),
                searchResponse = SearchResponse(DataResult.ready(emptyList()), "test provider", false)
            ),
            onUserIntent = {}
        )
    }
}

@PreviewOnKaroo2
@Composable
fun WaypointsListEmptyPreview() {
    AppTheme(darkTheme = true) {
        WaypointsListScreenContent(
            WaypointsListState(
                bookmarks = emptyList(),
                searchResponse = SearchResponse(DataResult.ready(emptyList()), "test provider", false)
            ),
            {}
        )
    }
}
//endregion

@Composable
fun WaypointsListScreen(
    viewModel: WaypointsListViewModel = hiltViewModel(),
) {
    val viewState = viewModel.viewState.collectAsState()

    NavigationOverlay {
        WaypointsListScreenContent(
            viewState = viewState.value,
            onUserIntent = viewModel::onUserIntent
        )
    }
}

@Composable
fun WaypointsListScreenContent(
    viewState: WaypointsListState,
    onUserIntent: (UserIntent) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 5.dp)
        ) {

            SearchWidget(
                searchResponse = viewState.searchResponse,
                onUserIntent = onUserIntent,
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .wrapContentHeight()
            )

            if (viewState.bookmarks.isNotEmpty()) {
                val itemsBookmarked = viewState.bookmarks
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp)
                        .padding(horizontal = 5.dp),
                ) {
                    items(count = itemsBookmarked.size, key = { itemsBookmarked[it].id }) { pos ->
                        WaypointBookmarkItem(
                            model = itemsBookmarked[pos],
                            onUserIntent = onUserIntent,
                        )
                    }
                }
            } else {
                InfoContentWidget(stringResource(R.string.message_no_bookmarks))
            }
        }
    }
}