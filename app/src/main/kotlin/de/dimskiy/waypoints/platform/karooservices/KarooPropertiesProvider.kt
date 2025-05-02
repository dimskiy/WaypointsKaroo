package de.dimskiy.waypoints.platform.karooservices

import de.dimskiy.waypoints.domain.model.DomainError.EnvironmentPropertiesError
import de.dimskiy.waypoints.domain.providers.EnvironmentPropertiesProvider
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.UserProfile.PreferredUnit.UnitType
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class KarooPropertiesProvider @Inject constructor(
    private val karooServiceProvider: KarooServiceProvider,
) : EnvironmentPropertiesProvider {

    override suspend fun isMeasureUnitMetric(): Boolean? = channelFlow {
        Timber.d("Distance unit not set - fetching...")
        karooServiceProvider.ensureConnected { service ->
            val isDistanceMetric = try {
                val userProfile = getUserProfile(service)
                Timber.d("User profile received: $userProfile")
                userProfile.preferredUnit.distance == UnitType.METRIC

            } catch (e: EnvironmentPropertiesError) {
                Timber.d("Error getting user profile: $e")
                null
            }

            send(isDistanceMetric)
        }
    }.first()

    private suspend fun getUserProfile(service: KarooSystemService): UserProfile {
        var consumerId: String? = null

        val userProfile = suspendCancellableCoroutine { continuation ->
            consumerId = service.addConsumer<UserProfile>(
                onEvent = {
                    continuation.resume(it)
                },
                onError = {
                    continuation.resumeWithException(EnvironmentPropertiesError(message = it))
                }
            )
            Timber.d("User profile consumer ($consumerId) added")
        }

        consumerId?.let(service::removeConsumer)
        Timber.d("User profile consumer ($consumerId) removed")

        return userProfile
    }
}