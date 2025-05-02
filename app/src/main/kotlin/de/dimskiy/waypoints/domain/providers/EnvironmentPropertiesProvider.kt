package de.dimskiy.waypoints.domain.providers

interface EnvironmentPropertiesProvider {

    suspend fun isMeasureUnitMetric(): Boolean?

}