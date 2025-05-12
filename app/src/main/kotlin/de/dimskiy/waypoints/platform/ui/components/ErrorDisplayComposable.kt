package de.dimskiy.waypoints.platform.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors
import de.dimskiy.waypoints.platform.errordisplay.di.ErrorDisplayModule

@Composable
fun <T> WithErrorDisplay(
    nonErrorStateKey: T,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    content: @Composable () -> Unit
) {
    val errorState = remember {
        EntryPointAccessors.fromApplication(
            context,
            ErrorDisplayModule.Accessor::class.java
        ).errorDisplayState()
    }
    val errors = errorState.observe().collectAsState(null)
    var isDisplayError by remember { mutableStateOf(errors.value != null) }

    LaunchedEffect(nonErrorStateKey) {
        isDisplayError = false
    }

    LaunchedEffect(errors.value) {
        isDisplayError = errors.value != null
    }

    val errorMessage = errors.value?.message
    if (isDisplayError && errorMessage != null) {
        ErrorContentWidget(errorMessage = errorMessage, modifier = modifier)
    } else {
        content()
    }
}