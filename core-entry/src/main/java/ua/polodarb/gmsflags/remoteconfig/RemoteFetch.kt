package ua.polodarb.gmsflags.remoteconfig

sealed interface RemoteFetch {
    data class Fetched(val raw: String) : RemoteFetch

    data object Failed : RemoteFetch
}
