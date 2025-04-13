package de.dimskiy.waypoints.platform.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ssjetpackcomposeswipeableview.SwipeAbleItemView
import com.example.ssjetpackcomposeswipeableview.SwipeDirection
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.model.Waypoint
import de.dimskiy.waypoints.platform.ui.PreviewOnKaroo2
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.model.UserIntent
import de.dimskiy.waypoints.platform.ui.theme.AppTheme

//region "Composable previews"
@Composable
@PreviewOnKaroo2
fun BookmarkedItemFull() {
    AppTheme(darkTheme = true) {
        DiscoveredListItem(
            model = Waypoint.Stored(
                id = 1,
                serverId = "11",
                name = "Awesome place",
                latitude = 10.0,
                longitude = 11.5,
                address = Waypoint.Address(
                    country = "DE",
                    city = "Munich",
                    zip = "12345",
                    street = "RosenheimerStr",
                    qualifier1 = "something",
                    qualifier2 = "nice",
                )
            ),
            onUserIntent = {}
        )
    }
}

@Composable
@PreviewOnKaroo2
fun BookmarkedItemShortAddress() {
    AppTheme {
        WaypointBookmarkItem(
            model = Waypoint.Stored(
                id = 1,
                serverId = "11",
                name = "Awesome place",
                latitude = 10.0,
                longitude = 11.5,
                address = Waypoint.Address(
                    country = "DE",
                    city = null,
                    zip = null,
                    street = "RosenheimerStr",
                    qualifier1 = "something",
                    qualifier2 = null,
                )
            ),
            onUserIntent = {}
        )
    }
}

@Composable
@PreviewOnKaroo2
fun SearchResultItemFull() {
    AppTheme(darkTheme = true) {
        DiscoveredListItem(
            model = Waypoint.Discovered(
                serverId = "11",
                name = "Awesome place",
                latitude = 10.0,
                longitude = 11.5,
                address = Waypoint.Address(
                    country = "DE",
                    city = "Munich",
                    zip = "12345",
                    street = "RosenheimerStr",
                    qualifier1 = "something",
                    qualifier2 = "nice",
                )
            ),
            onUserIntent = {}
        )
    }
}

@Composable
@PreviewOnKaroo2
fun SearchResultItemShortAddressBookmarked() {
    AppTheme(darkTheme = true) {
        WaypointBookmarkItem(
            model = Waypoint.Stored(
                id = 12,
                serverId = "11",
                name = "Awesome place",
                latitude = 10.0,
                longitude = 11.5,
                address = Waypoint.Address(
                    country = "DE",
                    city = null,
                    zip = null,
                    street = "RosenheimerStr",
                    qualifier1 = "something",
                    qualifier2 = null,
                )
            ),
            onUserIntent = {}
        )
    }
}
//endregion

@Composable
fun WaypointBookmarkItem(
    model: Waypoint,
    onUserIntent: (UserIntent) -> Unit,
    deleteIcon: Painter = rememberVectorPainter(Icons.Filled.Delete),
) {
    SwipeAbleItemView(
        leftViewIcons = arrayListOf(),
        rightViewIcons = arrayListOf(
            Triple(
                deleteIcon,
                MaterialTheme.colorScheme.onSurface,
                ""
            )
        ),
        leftViewBackgroundColor = MaterialTheme.colorScheme.errorContainer,
        rightViewBackgroundColor = MaterialTheme.colorScheme.errorContainer,
        swipeDirection = SwipeDirection.LEFT,
        height = 80.dp,
        onClick = { onUserIntent(UserIntent.DismissWaypoint(model)) },
    ) {
        val qualifiersInfo = remember { model.address.getQualifiersFormatted() }
        val addressInfo = remember { model.address.getFormatted() }

        Row {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .fillMaxHeight()
                    .width(10.dp)
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                },
                trailingContent = {
                    Image(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        contentDescription = "",
                    )
                },
                supportingContent = {
                    Column {
                        Text(
                            text = qualifiersInfo,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 1,
                        )

                        Text(
                            text = addressInfo,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true
                        )
                    }
                },
                shadowElevation = 4.dp,
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable { onUserIntent(UserIntent.ClickWaypoint(model)) }
            )
        }
    }
}

@Composable
fun DiscoveredListItem(
    model: Waypoint,
    onUserIntent: (UserIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val qualifiersInfo = remember { model.address.getQualifiersFormatted() }
    val addressInfo = remember { model.address.getFormatted() }

    ListItem(
        leadingContent = {
            Image(
                imageVector = if (model is Waypoint.Stored) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Filled.FavoriteBorder
                },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                contentDescription = stringResource(R.string.img_description_bookmarked_sign),
                modifier = Modifier.clickable(
                    enabled = true,
                    onClick = { onUserIntent(UserIntent.ToggleBookmark(model)) }
                )
            )
        },
        headlineContent = {
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = qualifiersInfo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                )

                Text(
                    text = addressInfo,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            }
        },
        shadowElevation = 4.dp,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onUserIntent(UserIntent.ClickWaypoint(model)) }
    )
}

@Composable
private fun LeadingIcon(
    showBookmarkIcon: Boolean,
    model: Waypoint,
    onUserIntent: (UserIntent) -> Unit,
) {
    if (showBookmarkIcon)
        Image(
            imageVector = if (model is Waypoint.Stored) {
                Icons.Filled.Favorite
            } else {
                Icons.Filled.FavoriteBorder
            },
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
            contentDescription = stringResource(R.string.img_description_bookmarked_sign),
            modifier = Modifier.clickable(
                enabled = true,
                onClick = {
                    onUserIntent(
                        UserIntent.ToggleBookmark(model)
                    )
                }
            )
        )

}