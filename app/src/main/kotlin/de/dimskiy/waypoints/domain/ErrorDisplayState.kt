package de.dimskiy.waypoints.domain

import de.dimskiy.waypoints.model.LocalError
import kotlinx.coroutines.flow.Flow

interface ErrorDisplayState {

    fun observeErrors(): Flow<ErrorDisplayModel>

    interface Receiver {

        fun notifyError(error: LocalError)

    }

    data class ErrorDisplayModel(
        val message: String,
        val exception: LocalError,
    )
}