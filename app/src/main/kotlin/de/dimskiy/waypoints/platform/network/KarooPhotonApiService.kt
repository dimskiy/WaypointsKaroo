package de.dimskiy.waypoints.platform.network

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.providers.photonservice.FeaturesCollectionDto
import de.dimskiy.waypoints.domain.providers.photonservice.PhotonApiService
import de.dimskiy.waypoints.platform.karooservices.KarooServiceProvider
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.HttpResponseState
import io.hammerhead.karooext.models.OnHttpResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

class KarooPhotonApiService @Inject constructor(
    private val karooServiceProvider: KarooServiceProvider,
    @ApplicationContext context: Context
) : PhotonApiService {

    private val baseUrl = context.getString(R.string.photon_api_base_url)

    private val gson = Gson()

    override suspend fun getFeaturedLocations(query: String, limit: Int): FeaturesCollectionDto {
        return callbackFlow {
            Timber.d("Simple locations discovery START...")
            karooServiceProvider.ensureConnected { service ->
                val url = "${baseUrl}api/?q=${query.encoded()}&limit=$limit"
                send(runRequest(url, service))
            }
            Timber.d("Simple locations discovery FINISHED")
            awaitCancellation()
        }.first()
    }

    override suspend fun getFeaturedLocationsWithGeo(
        query: String,
        limit: Int,
        zoom: Int,
        locationBiasScale: Double,
        lat: Double,
        lon: Double
    ): FeaturesCollectionDto {
        return callbackFlow {
            Timber.d("Geo-based locations discovery START...")
            karooServiceProvider.ensureConnected { service ->
                val url =
                    "${baseUrl}api/?q=${query.encoded()}&zoom=$zoom&location_bias_scale=$locationBiasScale&lat=$lat&lon=$lon&limit=$limit"
                send(runRequest(url, service))
            }
            Timber.d("Geo-based locations discovery FINISHED")
            awaitCancellation()
        }.first()
    }

    private fun String.encoded() = URLEncoder.encode(
        this,
        StandardCharsets.UTF_8.toString()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun runRequest(
        url: String,
        karooService: KarooSystemService
    ): FeaturesCollectionDto = suspendCancellableCoroutine<FeaturesCollectionDto> { continuation ->
        Timber.d("Request -> : $url")

        var consumerId: String? = null
        consumerId = karooService.addConsumer<OnHttpResponse>(
            params = OnHttpResponse.MakeHttpRequest(
                method = "GET",
                url = url,
                waitForConnection = false
            ),
            onError = {
                Timber.d("Request error: $it")
                consumerId?.let(karooService::removeConsumer)
                continuation.resumeWithException(RuntimeException(it))
            },
            onEvent = { progressResponse ->
                Timber.d("Request <- Response: ${progressResponse.state}")
                if (progressResponse.state is HttpResponseState.Complete) {
                    consumerId?.let(karooService::removeConsumer)
                    continuation.resume(mapResponseToDto(progressResponse)) {
                        Timber.d("Http session coroutine cancelled")
                    }
                }
            },
            onComplete = {
                Timber.d("Http consumer onComplete")
                consumerId?.let(karooService::removeConsumer)
            }
        )
    }

    private fun mapResponseToDto(
        progressResponse: OnHttpResponse
    ): FeaturesCollectionDto {
        return (progressResponse.state as HttpResponseState.Complete).let { response ->
            if (response.statusCode == 200 && response.body != null) {
                val stringBody = String(response.body!!, StandardCharsets.UTF_8)
                gson.fromJson(stringBody, FeaturesCollectionDto::class.java)

            } else throw RuntimeException("Http error: ${response.statusCode}, ${response.error}")
        }
    }
}