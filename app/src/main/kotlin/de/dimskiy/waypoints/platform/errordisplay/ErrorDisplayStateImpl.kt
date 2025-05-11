package de.dimskiy.waypoints.platform.errordisplay

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.ErrorDisplayState
import de.dimskiy.waypoints.model.LocalError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

fun <T> Flow<T>.catchWithErrorDisplay(errorDisplayReceiver: ErrorDisplayState.Receiver): Flow<T> =
    catch { error ->
        if (error is LocalError) {
            errorDisplayReceiver.notifyError(error)
        } else throw error
    }

@Singleton
class ErrorDisplayStateImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ErrorDisplayState, ErrorDisplayState.Receiver {

    private val errors = MutableSharedHoldingFlow<ErrorDisplayState.ErrorDisplayModel>()

    override fun observeErrors(): Flow<ErrorDisplayState.ErrorDisplayModel> = errors

    override fun notifyError(exception: LocalError) {
        val errorMessage = when (exception) {
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