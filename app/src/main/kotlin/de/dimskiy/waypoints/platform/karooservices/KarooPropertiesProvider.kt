package de.dimskiy.waypoints.platform.karooservices

import de.dimskiy.waypoints.domain.providers.EnvironmentPropertiesProvider
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.UserProfile.PreferredUnit.UnitType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class KarooPropertiesProvider @Inject constructor(
    private val karooServiceProvider: KarooServiceProvider,
) : EnvironmentPropertiesProvider {

    override suspend fun isMeasureUnitMetric(): Boolean =
        karooServiceProvider.observeEvent<UserProfile>(
            eventParams = UserProfile.Params
        ).map { profile ->
            Timber.d("User profile received: $profile")
            profile.preferredUnit.distance == UnitType.METRIC
        }.first()
}