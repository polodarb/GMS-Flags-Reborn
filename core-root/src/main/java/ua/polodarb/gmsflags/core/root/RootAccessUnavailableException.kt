package ua.polodarb.gmsflags.core.root

class RootAccessUnavailableException(
    cause: Throwable? = null,
) : IllegalStateException("Root access is unavailable", cause)
