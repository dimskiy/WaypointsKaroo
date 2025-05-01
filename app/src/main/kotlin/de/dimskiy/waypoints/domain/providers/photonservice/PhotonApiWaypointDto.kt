package de.dimskiy.waypoints.domain.providers.photonservice

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FeaturesCollectionDto(
    @SerializedName("features") val features: List<FeatureDto>
)

@Keep
data class FeatureDto(
    @SerializedName("geometry") val geometry: GeometryDto,
    @SerializedName("properties") val properties: PropertiesDto
)

@Keep
data class GeometryDto(
    @SerializedName("coordinates") val coordinates: List<Double>
)

@Keep
data class PropertiesDto(
    @SerializedName("osm_id") val serverId: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("countrycode") val country: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("housenumber") val houseNumber: String? = null,
    @SerializedName("postcode") val postcode: String? = null,
    @SerializedName("osm_key") val qualifier1: String? = null,
    @SerializedName("osm_value") val qualifier2: String? = null
)