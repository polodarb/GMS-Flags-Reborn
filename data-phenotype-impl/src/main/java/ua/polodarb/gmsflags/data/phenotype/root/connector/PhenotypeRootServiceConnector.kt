package ua.polodarb.gmsflags.data.phenotype.root.connector

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import ua.polodarb.gmsflags.data.phenotype.root.IPhenotypeRootService
import ua.polodarb.gmsflags.core.root.RootAccessManager
import ua.polodarb.gmsflags.core.root.RootAccessUnavailableException
import ua.polodarb.gmsflags.domain.error.AppError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class PhenotypeRootServiceConnector(
    context: Context,
    private val rootAccessManager: RootAccessManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val connectionTimeoutMillis: Long = DEFAULT_ROOT_SERVICE_TIMEOUT_MS,
    private val operationTimeoutMillis: Long = DEFAULT_ROOT_OPERATION_TIMEOUT_MS,
) {
    private val applicationContext = context.applicationContext
    private val connectionMutex = Mutex()
    private val operationMutex = Mutex()

    @Volatile
    private var cachedService: IPhenotypeRootService? = null

    @Volatile
    private var activeConnection: ServiceConnection? = null

    suspend fun <T> call(block: (IPhenotypeRootService) -> T): Result<T> {
        rootAccessManager.requestAccess().onFailure { error ->
            if (error is CancellationException) throw error
            return Result.failure(AppError.RootUnavailable)
        }

        return try {
            Result.success(operationMutex.withLock {
                val service = connectedService()
                withTimeout(operationTimeoutMillis) {
                    runInterruptible(dispatcher) { block(service) }
                }
            })
        } catch (error: TimeoutCancellationException) {
            clearDeadConnection()
            Result.failure(AppError.RootServiceUnavailable(error))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            if (cachedService?.asBinder()?.isBinderAlive != true) clearDeadConnection()
            Result.failure(error.toAppError())
        }
    }

    private suspend fun connectedService(): IPhenotypeRootService = connectionMutex.withLock {
        cachedService?.takeIf { it.asBinder().isBinderAlive }?.let { return@withLock it }
        clearDeadConnection()

        val binder = withTimeout(connectionTimeoutMillis) {
            suspendCancellableCoroutine<IBinder> { continuation ->
                val serviceConnection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        if (service == null && continuation.isActive) {
                            continuation.resumeWithException(
                                RootServiceConnectionException(
                                    "Phenotype root service returned no binder"
                                )
                            )
                        } else if (service != null && continuation.isActive) {
                            continuation.resume(service)
                        }
                    }

                    override fun onNullBinding(name: ComponentName?) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                RootServiceConnectionException(
                                    "Phenotype root service returned no binder"
                                )
                            )
                        }
                    }

                    override fun onBindingDied(name: ComponentName?) {
                        cachedService = null
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                RootServiceConnectionException(
                                    "Phenotype root service binding died"
                                )
                            )
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        cachedService = null
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                RootServiceConnectionException(
                                    "Phenotype root service disconnected"
                                )
                            )
                        }
                    }
                }
                activeConnection = serviceConnection
                continuation.invokeOnCancellation {
                    runCatching { RootService.unbind(serviceConnection) }
                    if (activeConnection === serviceConnection) activeConnection = null
                }
                RootService.bind(
                    Intent(applicationContext, PhenotypeRootService::class.java),
                    serviceConnection,
                )
            }
        }

        IPhenotypeRootService.Stub.asInterface(binder).also { cachedService = it }
    }

    private fun clearDeadConnection() {
        cachedService = null
        activeConnection?.let { connection ->
            runCatching { RootService.unbind(connection) }
        }
        activeConnection = null
    }

    private companion object {
        const val DEFAULT_ROOT_SERVICE_TIMEOUT_MS = 30_000L
        const val DEFAULT_ROOT_OPERATION_TIMEOUT_MS = 20_000L
    }
}

private fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is RootAccessUnavailableException -> AppError.RootUnavailable
    is TimeoutCancellationException,
    is RootServiceConnectionException -> AppError.RootServiceUnavailable(this)
    is SecurityException -> AppError.AccessDenied
    else -> AppError.SystemDataUnavailable(this)
}

private class RootServiceConnectionException(message: String) : IllegalStateException(message)
