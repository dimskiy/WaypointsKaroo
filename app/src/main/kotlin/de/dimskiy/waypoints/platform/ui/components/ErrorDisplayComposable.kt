package de.dimskiy.waypoints.platform.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.dimskiy.waypoints.domain.ErrorDisplayState

@Composable
fun <T> WithErrorDisplay(
    normalStateData: T,
    errorState: ErrorDisplayState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val errors = errorState.observeErrors().collectAsState(null)
    var isDisplayError by remember { mutableStateOf(errors.value != null) }

    LaunchedEffect(normalStateData) {
        isDisplayError = false
    }

    LaunchedEffect(errors.value) {
        isDisplayError = errors.value != null
    }

    if (isDisplayError) {
        errors.value?.message?.let {
            ErrorContentWidget(errorMessage = it, modifier = modifier)
        }
    } else {
        content()
    }
}