package de.dimskiy.waypoints.domain.model

sealed class DomainError : Throwable() {

    data class NetworkError(
        val wrappedException: Throwable? = null,
        override val message: String? = wrappedException?.message
    ) : DomainError()

    data class LocationServiceError(
        val wrappedException: Throwable? = null,
        override val message: String? = wrappedException?.message
    ) : DomainError()
}