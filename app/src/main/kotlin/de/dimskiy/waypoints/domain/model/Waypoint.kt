package de.dimskiy.waypoints.domain.model

sealed class Waypoint {
    open val id: Int = -1
    abstract val serverId: String
    abstract val name: String
    abstract val address: Address
    abstract val latitude: Double
    abstract val longitude: Double

    data class Discovered(
        override val serverId: String,
        override val name: String,
        override val address: Address,
        override val latitude: Double,
        override val longitude: Double
    ) : Waypoint()

    data class Stored(
        override val id: Int,
        override val serverId: String,
        override val name: String,
        override val address: Address,
        override val latitude: Double,
        override val longitude: Double
    ) : Waypoint()

    // TODO: Use the separate Table for qualifiers
    data class Address(
        val country: String?,
        val city: String?,
        val zip: String?,
        val street: String?,
        val qualifier1: String?,
        val qualifier2: String?
    ) {
        fun getQualifiersFormatted(): String = listOfNotNull(qualifier1, qualifier2)
            .joinToString(prefix = "#", separator = " #")

        fun getFormatted(): String {
            val countryAndCity = listOfNotNull(country, city)
                .takeIf(List<*>::isNotEmpty)
                ?.joinToString(prefix = "[", postfix = "]", separator = ": ")

            return listOfNotNull(countryAndCity, street)
                .joinToString(separator = " ")
        }
    }
}