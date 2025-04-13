package de.dimskiy.waypoints.platform.network

import de.dimskiy.waypoints.domain.providers.photonservice.FeaturesCollectionDto
import de.dimskiy.waypoints.domain.providers.photonservice.PhotonApiService
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitPhotonApiService : PhotonApiService {

    @GET("api/")
    override suspend fun getFeaturedLocations(
        @Query("q") query: String,
        @Query("limit") limit: Int
    ): FeaturesCollectionDto

    @GET("api/")
    override suspend fun getFeaturedLocationsWithGeo(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("zoom") zoom: Int,
        @Query("location_bias_scale") locationBiasScale: Double,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): FeaturesCollectionDto
}