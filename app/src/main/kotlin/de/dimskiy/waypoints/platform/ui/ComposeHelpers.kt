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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import timber.log.Timber

@Preview(name = "Karoo 2", device = "spec:width=480px,height=800px,dpi=295")
annotation class PreviewOnKaroo2

@Composable
fun GetInputLanguageCode(): String {
    val context = LocalContext.current
    val inputManager = remember {
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    var inputLanguageCode by remember { mutableStateOf(Locale.current.toLanguageTag()) }

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