package de.dimskiy.waypoints.platform.errordisplay

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.dimskiy.waypoints.DataResult
import de.dimskiy.waypoints.R
import de.dimskiy.waypoints.domain.UserInformerState
import de.dimskiy.waypoints.model.LocalException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : DataResult<*>> Flow<T>.flatMapWithUserInformer(errorDisplayReceiver: UserInformerState.Receiver): Flow<T> =
    flatMapLatest { dataResult ->
        when {
            dataResult is DataResult.Error<*> && dataResult.error is LocalException -> {
                errorDisplayReceiver.notifyError(dataResult.error)
                emptyFlow()
            }

            dataResult is DataResult.Error<*> -> {
                val error = dataResult.getErrorIfAny()
                    ?: IllegalStateException("Cannot find error in result")
                throw error
            }

            else -> this
        }
    }

fun <T> Flow<T>.catchWithUserInformer(errorDisplayReceiver: UserInformerState.Receiver): Flow<T> =
    catch { exception ->
        if (exception is LocalException) {
            errorDisplayReceiver.notifyError(exception)
        } else throw exception
    }

@Singleton
class UserInformerStateImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserInformerState, UserInformerState.Receiver {

    private val exceptions = MutableSharedHoldingFlow<UserInformerState.Exception>()

    override fun observe(): Flow<UserInformerState.Exception> = exceptions

    override fun notifyError(exception: LocalException) {
        exceptions.tryEmit(
            UserInformerState.Exception(message = getExceptionText(exception))
        )
    }

    private fun getExceptionText(exception: LocalException): String = when (exception) {
        is LocalException.KarooServiceException -> context.getString(R.string.error_msg_karoo_service_unavailable)
        is LocalException.LocationServiceException -> context.getString(R.string.error_msg_no_location)
        is LocalException.NetworkException -> context.getString(R.string.error_msg_network_timeout)
    }
}