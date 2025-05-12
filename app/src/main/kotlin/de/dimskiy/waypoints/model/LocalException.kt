package de.dimskiy.waypoints.model

sealed class LocalException : Throwable() {

    data class NetworkException(
        val wrappedException: Throwable? = null,
        override val message: String? = wrappedException?.message
    ) : LocalException()

    data class LocationServiceException(
        val wrappedException: Throwable? = null,
        override val message: String? = wrappedException?.message
    ) : LocalException()

    data object KarooServiceException : LocalException() {
        private fun readResolve(): Any = KarooServiceException
    }
}