package de.dimskiy.waypoints.platform.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.dimskiy.waypoints.domain.providers.SettingsProvider
import de.dimskiy.waypoints.platform.settings.SettingsProviderImpl
import de.dimskiy.waypoints.platform.storage.AppDatabase
import de.dimskiy.waypoints.platform.storage.WaypointsDao
import de.dimskiy.waypoints.platform.storage.migrations.MigrationFrom1to2
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindSettingsProvider(impl: SettingsProviderImpl): SettingsProvider

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "location_settings")

        @Provides
        @Singleton
        fun provideWaypointsDao(@ApplicationContext context: Context): WaypointsDao {
            val db = Room.databaseBuilder(
                context = context,
                klass = AppDatabase::class.java,
                name = AppDatabase.DB_NAME
            )
                .addMigrations(MigrationFrom1to2)
                .build()

            return db.waypointDao()
        }

        @Provides
        @Singleton
        fun providePrefsDatastore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.dataStore
        }
    }
}