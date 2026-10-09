package ua.polodarb.gmsflags.presentation.feature.settings.backup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.domain.backup.BackupRestoreResult
import ua.polodarb.gmsflags.domain.backup.FlagsBackup
import ua.polodarb.gmsflags.domain.backup.FlagsBackupCodec
import ua.polodarb.gmsflags.domain.backup.FlagsBackupService
import ua.polodarb.gmsflags.presentation.feature.settings.R

internal data class BackupState(
    val busy: Boolean = false,
    val backup: FlagsBackup? = null,
    val availableApps: Set<String> = emptySet(),
    val message: Int? = null,
    val result: BackupRestoreResult? = null,
    val savedFileName: String? = null,
    val summaryVisible: Boolean = false,
)

internal class BackupViewModel(
    private val service: FlagsBackupService,
    private val codec: FlagsBackupCodec,
    private val documents: BackupDocumentStore,
    private val exports: BackupExportStore,
    initialUri: String?,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val mutableState = MutableStateFlow(BackupState())
    val state = mutableState.asStateFlow()
    private val saveRequests = Channel<Unit>(Channel.BUFFERED)
    val saveRequest = saveRequests.receiveAsFlow()
    private var saving = false

    init {
        val pendingId = savedStateHandle.get<String>(PENDING_EXPORT)
        val targetUri = savedStateHandle.get<String>(TARGET_URI)
        if (pendingId != null) {
            mutableState.value = BackupState(busy = true)
            if (targetUri != null) {
                save(targetUri)
            } else if (savedStateHandle.get<Boolean>(PICKER_LAUNCHED) != true) {
                saveRequests.trySend(Unit)
            }
        } else if (initialUri != null) {
            load(initialUri)
        }
    }

    fun export() {
        if (state.value.busy) {
            return
        }
        mutableState.value = BackupState(busy = true)
        viewModelScope.launch {
            try {
                val backup = service.export()
                if (backup.flagCount == 0) {
                    mutableState.value = BackupState(message = R.string.backup_empty)
                } else {
                    val xml = withContext(backgroundDispatcher) { codec.encode(backup) }
                    savedStateHandle[PENDING_EXPORT] = exports.prepare(xml)
                    savedStateHandle[PICKER_LAUNCHED] = false
                    saveRequests.send(Unit)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableState.value = BackupState(message = R.string.backup_export_failed)
            }
        }
    }

    fun pickerLaunched() {
        savedStateHandle[PICKER_LAUNCHED] = true
    }

    fun dismissSummary() {
        mutableState.value = state.value.copy(summaryVisible = false)
    }

    fun save(uri: String?) {
        if (saving) {
            return
        }
        val id = savedStateHandle.get<String>(PENDING_EXPORT)
        if (uri == null) {
            clearPendingExport()
            mutableState.value = BackupState()
            if (id != null) {
                viewModelScope.launch { discardExport(id) }
            }
            return
        }
        savedStateHandle[TARGET_URI] = uri
        saving = true
        mutableState.value = BackupState(busy = true)
        viewModelScope.launch {
            try {
                val xml = exports.read(checkNotNull(id))
                val backup = withContext(backgroundDispatcher) { codec.decode(xml) }
                documents.write(uri, xml)
                val name = try {
                    documents.displayName(uri)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
                mutableState.value = BackupState(
                    backup = backup,
                    message = R.string.backup_saved,
                    savedFileName = name ?: "gms-flags-backup.gmsbackup",
                    summaryVisible = true,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.value = BackupState(message = R.string.backup_save_failed)
            } finally {
                saving = false
                clearPendingExport()
                if (id != null) {
                    discardExport(id)
                }
            }
        }
    }

    private fun clearPendingExport() {
        savedStateHandle.remove<String>(PENDING_EXPORT)
        savedStateHandle.remove<String>(TARGET_URI)
        savedStateHandle.remove<Boolean>(PICKER_LAUNCHED)
    }

    private suspend fun discardExport(id: String) {
        try {
            exports.delete(id)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {}
    }

    fun load(uri: String) {
        if (state.value.busy) {
            return
        }
        mutableState.value = BackupState(busy = true)
        viewModelScope.launch {
            try {
                val content = documents.read(uri)
                val backup = withContext(backgroundDispatcher) { codec.decode(content) }
                val available = service.availableApps()
                mutableState.value = BackupState(backup = backup, availableApps = available)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableState.value = BackupState(message = R.string.backup_import_failed)
            }
        }
    }

    fun restore() {
        val current = state.value
        val backup = current.backup ?: return
        if (current.busy) {
            return
        }
        mutableState.value = current.copy(busy = true, message = null, result = null)
        viewModelScope.launch {
            try {
                val result = service.restore(backup)
                mutableState.value = current.copy(result = result)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.value = current.copy(message = R.string.backup_restore_failed)
            }
        }
    }

    private companion object {
        const val PENDING_EXPORT = "backup.pendingExport"
        const val TARGET_URI = "backup.targetUri"
        const val PICKER_LAUNCHED = "backup.pickerLaunched"
    }
}
