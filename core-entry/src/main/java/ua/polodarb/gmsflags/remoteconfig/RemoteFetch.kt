package ua.polodarb.gmsflags.remoteconfig

sealed interface RemoteFetch {
    data class Fetched(val raw: String) : RemoteFetch

    data object Failed : RemoteFetch
}

sealed interface FetchFailure {
    data class Thrown(val error: Throwable) : FetchFailure

    data object Reported : FetchFailure
}

internal const val REMOTE_CONFIG_LOG_PREFIX = "remote_config"
