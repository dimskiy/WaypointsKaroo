package de.dimskiy.waypoints.domain.providers

import de.dimskiy.waypoints.domain.model.ReportingModel

interface ReportingProvider {

    fun report(model: ReportingModel): ReportingProvider

    fun logError(error: Throwable): ReportingProvider
}