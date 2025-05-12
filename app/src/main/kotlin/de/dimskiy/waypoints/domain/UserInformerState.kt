package de.dimskiy.waypoints.domain

import de.dimskiy.waypoints.model.LocalException
import kotlinx.coroutines.flow.Flow

interface UserInformerState {

    fun observe(): Flow<Exception>

    interface Receiver {

        fun notifyError(exception: LocalException)

    }

    data class Exception(val message: String)
}