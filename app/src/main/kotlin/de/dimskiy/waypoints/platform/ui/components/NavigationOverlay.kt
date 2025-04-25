package de.dimskiy.waypoints.platform.ui.components

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.dimskiy.waypoints.R

@Composable
fun NavigationOverlay(
    backDispatcher: OnBackPressedDispatcher? = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
    content: @Composable () -> Unit
) {
    content()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        FloatingActionButton(
            onClick = { backDispatcher?.onBackPressed() },
            modifier = Modifier
                .width(52.dp)
                .height(62.dp)
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(topEnd = 25.dp, bottomEnd = 25.dp),
            elevation = FloatingActionButtonDefaults.elevation(0.dp),
            containerColor = colorResource(R.color.GreyBlue),
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.description_navigate_back),
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(16.dp)
            )
        }
    }
}