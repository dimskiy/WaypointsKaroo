package de.dimskiy.waypoints.model

sealed class LocalError : Throwable() {

    data class NetworkError(
        val wrappedException: Throwable? = null,
        override val message: String? = wrappedException?.message
    ) : LocalError()

    data class LocationServiceError(
        val wrappedException: Throwable? = null,
        override val message: String? = wrappedException?.message
    ) : LocalError()

    data object KarooServiceError: LocalError() {
        private fun readResolve(): Any = KarooServiceError
    }
}