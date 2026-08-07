package ua.polodarb.gmsflags.core.root

interface RootAccessManager {
    suspend fun requestAccess(): Result<Unit>
}
