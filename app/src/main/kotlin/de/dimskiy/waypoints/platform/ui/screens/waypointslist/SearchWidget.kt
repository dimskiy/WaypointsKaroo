package de.dimskiy.waypoints.platform.ui.screens.waypointslist

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.platform.ui.GetInputLanguageCode
import de.dimskiy.waypoints.platform.ui.components.DiscoveredListItem
import de.dimskiy.waypoints.platform.ui.components.ErrorContentWidget
import de.dimskiy.waypoints.platform.ui.components.InfoContentWidget
import de.dimskiy.waypoints.platform.ui.components.LoadingContentWidget
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.SearchResponse
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.UserIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWidget(
    searchResponse: SearchResponse,
    onUserIntent: (UserIntent) -> Unit,
    modifier: Modifier = Modifier,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    backDispatcher: OnBackPressedDispatcher? = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
) {
    var query by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val inputLanguageCode = GetInputLanguageCode()

    val searchAction = {
        keyboardController?.hide()
        onUserIntent(
            UserIntent.PerformSearch(query, inputLanguageCode)
        )
    }

    SearchBar(
        query = query,
        onQueryChange = { query = it },
        onSearch = { searchAction() },
        active = isSearchActive,
        onActiveChange = { isSearchActive = it },
        placeholder = {
            Text(
                text = stringResource(R.string.widget_search_placeholder),
                modifier = Modifier.alpha(0.7f),
            )
        },
        leadingIcon = {
            LeadingIcon(isSearchActive = isSearchActive) {
                if (isSearchActive) {
                    query = ""
                    searchAction()
                    backDispatcher?.onBackPressed()
                } else {
                    isSearchActive = true
                }
            }
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSearchActive && query.isNotEmpty()) {
                    Image(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.clickable {
                            query = ""
                            keyboardController?.show()
                        }
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        SearchContent(
            searchResponse = searchResponse,
            onUserIntent = onUserIntent,
        )
    }
}

@Composable
private fun LeadingIcon(isSearchActive: Boolean, onClick: () -> Unit) {
    Image(
        imageVector = if (isSearchActive) Icons.AutoMirrored.Default.ArrowBack else Icons.Rounded.Search,
        contentDescription = if (isSearchActive) {
            stringResource(R.string.back_button_description)
        } else stringResource(R.string.search_button_description),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
        modifier = Modifier.clickable(
            enabled = true,
            onClick = onClick
        )
    )
}

@Composable
private fun SearchContent(
    searchResponse: SearchResponse,
    onUserIntent: (UserIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = searchResponse.geoSearchEnabled,
                onCheckedChange = { onUserIntent(UserIntent.ToggleGeoSearch(it)) }
            )
            Column {
                Text(
                    text = stringResource(R.string.label_search_by_location),
                    modifier = Modifier.padding(start = 10.dp)
                )

                Text(
                    text = stringResource(
                        R.string.label_search_by_location_explain,
                        searchResponse.providerName
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .alpha(0.4f)
                )
            }
        }

        Box {
            when (searchResponse.dataResult) {
                is DataResult.Error -> {
                    val errorMsg = searchResponse.dataResult.error.message
                        ?: stringResource(R.string.error_msg_generic)

                    ErrorContentWidget(errorMsg)
                }

                is DataResult.Loading -> {
                    LoadingContentWidget(searchResponse.dataResult)
                }

                is DataResult.Ready -> {
                    SearchResultList(
                        data = searchResponse.dataResult.data,
                        onUserIntent = onUserIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultList(
    data: List<Waypoint>,
    onUserIntent: (UserIntent) -> Unit
) {
    if (data.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(data.size) { pos ->
                DiscoveredListItem(
                    model = data[pos],
                    onUserIntent = onUserIntent
                )
            }
        }
    } else {
        InfoContentWidget(message = stringResource(R.string.message_nothing_gound))
    }
}