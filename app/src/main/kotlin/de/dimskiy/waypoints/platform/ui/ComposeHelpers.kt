package de.dimskiy.waypoints.platform.ui

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.dimskiy.waypoints.R
import timber.log.Timber
import java.text.DecimalFormat
import java.util.Locale

@Preview(name = "Karoo 2", device = "spec:width=480px,height=800px,dpi=295")
annotation class PreviewOnKaroo2

@Composable
fun GetInputLanguageCode(): String {
    val context = LocalContext.current
    val inputManager = remember {
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    var inputLanguageCode by remember { mutableStateOf(androidx.compose.ui.text.intl.Locale.current.toLanguageTag()) }

    LaunchedEffect(inputManager.currentInputMethodSubtype) {
        val languageCode = inputManager.currentInputMethodSubtype?.locale?.replace('_', '-')
        if (languageCode != null) {
            Timber.d("Input language detected: $languageCode")
            inputLanguageCode = languageCode
        } else {
            Timber.d("Cannot detect input language, using last value: $inputLanguageCode")
        }
    }

    return inputLanguageCode
}

@Composable
fun GetDistanceFormatted(distanceKm: Double, locale: Locale): String {
    val isImperialUnits = remember { locale == Locale.US || locale == Locale.UK }

    return when {
        isImperialUnits && distanceKm >= 0.621371 -> {
            val miles = distanceKm * 0.621371
            val mileFormat = DecimalFormat("#,##0.#")
            stringResource(R.string.distance_miles_formatted, mileFormat.format(miles))
        }

        isImperialUnits -> {
            val feet = distanceKm * 3280.84
            val feetFormat = DecimalFormat("#,##0")
            stringResource(R.string.distance_feet_formatted, feetFormat.format(feet))
        }

        distanceKm >= 1 -> {
            val kmFormat = DecimalFormat("#,##0.#")
            stringResource(R.string.distance_km_formatted, kmFormat.format(distanceKm))
        }

        else -> {
            val meters = (distanceKm * 1000).toInt()
            val meterFormat = DecimalFormat("#,##0")
            stringResource(R.string.distance_meters_formatted, meterFormat.format(meters))
        }
    }
}