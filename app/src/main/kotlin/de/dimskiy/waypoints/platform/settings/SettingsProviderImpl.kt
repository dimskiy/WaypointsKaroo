package de.dimskiy.waypoints.platform.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.providers.SettingsProvider
import de.dimskiy.waypoints.platform.di.BaseModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SettingsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @BaseModule.DispatcherIO private val coroutinesDispatcher: CoroutineDispatcher
) : SettingsProvider {

    private val Context.dataStore by preferencesDataStore(name = "location_settings")

    private val json = Gson()

    override fun observeLastLocation(): Flow<DeviceLocation?> = context.dataStore.data.map { data ->
        data[LAST_LOCATION_KEY]?.let { valueString ->
            val decoded = json.fromJson<DeviceLocation>(valueString, DeviceLocation::class.java)
            Timber.d("Reading $valueString to $decoded")
            decoded
        }
    }.flowOn(coroutinesDispatcher)

    override suspend fun saveLocation(lastLocation: DeviceLocation) {
        withContext(coroutinesDispatcher) {
            val encoded = json.toJson(lastLocation)

            Timber.d("Writing $lastLocation as $encoded")

            context.dataStore.edit { prefs ->
                prefs[LAST_LOCATION_KEY] = encoded
            }
        }
    }

    override fun observeGeoSearchEnabled(): Flow<Boolean> = context.dataStore.data.map { data ->
        data[GEO_SEARCH_ENABLED_KEY] == true
    }

    override suspend fun setGeoSearchEnabled(isEnabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GEO_SEARCH_ENABLED_KEY] = isEnabled
        }
    }

    companion object {
        private val LAST_LOCATION_KEY = stringPreferencesKey("lastLocationKey")
        private val GEO_SEARCH_ENABLED_KEY = booleanPreferencesKey("geosearchEnabledKey")
    }
}
