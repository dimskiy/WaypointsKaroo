package de.dimskiy.waypoints

sealed class DataResult<out T>() {
    open val data: T? = null

    fun isReady(): Boolean = this is Ready

    fun isError(): Boolean = this is Error

    fun isLoading(): Boolean = this is Loading

    fun <NEW_TYPE> mapResult(loadingMessage: String? = null, mapper: (T) -> NEW_TYPE): DataResult<NEW_TYPE> {
        return when (this) {
            is Error -> error(error)
            is Loading -> loading(loadingMessage)
            is Ready -> ready(mapper(data))
        }
    }

    suspend fun <NEW_TYPE> mapResultSuspend(loadingMessage: String? = null, mapper: suspend (T) -> NEW_TYPE): DataResult<NEW_TYPE> {
        return when (this) {
            is Error -> error(error)
            is Loading -> loading(loadingMessage)
            is Ready -> ready(mapper(data))
        }
    }

    fun getErrorIfAny(): Throwable? = if (this is Error) error else null

    data class Loading(val message: String? = null) : DataResult<Nothing>()

    data class Error<T>(val error: Throwable?) : DataResult<T>()

    data class Ready<T>(override val data: T) : DataResult<T>()

    companion object {

        fun loading(message: String? = null) = Loading(message)

        fun <T> ready(data: T) = Ready(data)

        fun <T> error(error: Throwable?) = Error<T>(error)
    }
}