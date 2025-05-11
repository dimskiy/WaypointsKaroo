package de.dimskiy.waypoints.platform.errordisplay

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.ErrorDisplayState
import de.dimskiy.waypoints.model.LocalError
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorDisplayStateImpl @Inject constructor(
    @ApplicationContext private val context: Context
): ErrorDisplayState, ErrorDisplayState.Receiver {

    private val errors = MutableSharedFlow<ErrorDisplayState.ErrorDisplayModel>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun observeErrors(): Flow<ErrorDisplayState.ErrorDisplayModel> = errors

    override fun notifyError(exception: LocalError) {
        val errorMessage = when(exception) {
            is LocalError.KarooServiceError -> context.getString(R.string.error_msg_karoo_service_unavailable)
            is LocalError.LocationServiceError -> context.getString(R.string.error_msg_no_location)
            is LocalError.NetworkError -> context.getString(R.string.error_msg_network_timeout)
        }

        errors.tryEmit(
            ErrorDisplayState.ErrorDisplayModel(
                message = errorMessage,
                exception = exception
            )
        )
    }
}