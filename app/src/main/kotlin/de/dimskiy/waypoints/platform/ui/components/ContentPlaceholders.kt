package de.dimskiy.waypoints.platform.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.platform.ui.PreviewOnKaroo2
import de.dimskiy.waypoints.platform.ui.theme.AppTheme

//region "Composable previews"
@PreviewOnKaroo2
@Composable
fun InfoContentPreview() {
    AppTheme(darkTheme = false) {
        InfoContentWidget("Some messageeeeeeeeeeeee with reaaaaaaaaaaaaaaaaaaally long teeeeeext")
    }
}

@PreviewOnKaroo2
@Composable
fun ErrorContentPreview() {
    AppTheme(darkTheme = false) {
        ErrorContentWidget("Some error")
    }
}

@PreviewOnKaroo2
@Composable
fun LoadingWithMessagePreview() {
    AppTheme(darkTheme = true) {
        LoadingContentWidget(DataResult.loading("Loading message"))
    }
}

@PreviewOnKaroo2
@Composable
fun LoadingNoMessagePreview() {
    AppTheme(darkTheme = true) {
        LoadingContentWidget(DataResult.loading())
    }
}
//endregion

@Composable
fun LoadingContentWidget(model: DataResult.Loading? = null, modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 5.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.TopCenter)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = model?.message.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoContentWidget(message: String, modifier: Modifier = Modifier) {
    ContentWidget(
        icon = Icons.Outlined.Info,
        message = message,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun ErrorContentWidget(errorMessage: String, modifier: Modifier = Modifier) {
    ContentWidget(
        icon = Icons.Outlined.Warning,
        iconColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
        message = errorMessage,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun ContentWidget(
    icon: ImageVector,
    iconColorFilter: ColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
    message: String,
    modifier: Modifier
) {
    Surface(modifier = modifier.padding(top = 10.dp)) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 5.dp, horizontal = 10.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        imageVector = icon,
                        contentDescription = "",
                        colorFilter = iconColorFilter,
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.TopCenter)
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}