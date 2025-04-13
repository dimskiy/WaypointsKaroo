package de.dimskiy.waypoints.domain.providers.photonservice

interface PhotonApiService {

    /*'api/'*/
    suspend fun getFeaturedLocations(
        /*'q'*/ query: String,
        /*'limit'*/ limit: Int
    ): FeaturesCollectionDto

    /*'api/'*/
    suspend fun getFeaturedLocationsWithGeo(
        /*'q'*/ query: String,
        /*'limit'*/ limit: Int,
        /*'zoom'*/ zoom: Int,
        /*location_bias_scale*/ locationBiasScale: Double,
        /*'lat'*/ lat: Double,
        /*'lon'*/ lon: Double,
    ): FeaturesCollectionDto
}