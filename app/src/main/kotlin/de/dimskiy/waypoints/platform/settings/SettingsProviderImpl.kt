package de.dimskiy.waypoints.platform.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import de.dimskiy.waypoints.domain.model.DeviceLocation
import de.dimskiy.waypoints.domain.providers.SettingsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class SettingsProviderImpl @Inject constructor(
    private val prefsDatastore: DataStore<Preferences>
) : SettingsProvider {

    private val json = Gson()

    override fun observeLastLocation(): Flow<DeviceLocation?> = prefsDatastore.data.map { data ->
        data[LAST_LOCATION_KEY]?.let { valueString ->
            val decoded = json.fromJson<DeviceLocation>(valueString, DeviceLocation::class.java)
            Timber.d("Reading $valueString to $decoded")
            decoded
        }
    }

    override suspend fun saveLocation(lastLocation: DeviceLocation) {
        prefsDatastore.edit { prefs ->
            val encoded = json.toJson(lastLocation)
            Timber.d("Writing $lastLocation as $encoded")
            prefs[LAST_LOCATION_KEY] = encoded
        }
    }

    override fun observeGeoSearchEnabled(): Flow<Boolean> = prefsDatastore.data.map { data ->
        data[GEO_SEARCH_ENABLED_KEY] == true
    }

    override suspend fun setGeoSearchEnabled(isEnabled: Boolean) {
        prefsDatastore.edit { prefs ->
            Timber.d("Setting geo-search enabled == $isEnabled")
            prefs[GEO_SEARCH_ENABLED_KEY] = isEnabled
        }
    }

    companion object {
        private val LAST_LOCATION_KEY = stringPreferencesKey("lastLocationKey")
        private val GEO_SEARCH_ENABLED_KEY = booleanPreferencesKey("geosearchEnabledKey")
    }
}
