package ua.polodarb.gmsflags.core.root.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.core.root.LibsuRootManager
import ua.polodarb.gmsflags.core.root.LibsuRootShell
import ua.polodarb.gmsflags.core.root.RootAccessManager
import ua.polodarb.gmsflags.core.root.RootCommandExecutor
import ua.polodarb.gmsflags.core.root.RootShell

val coreRootModule = module {
    single<RootShell> { LibsuRootShell() }
    single { LibsuRootManager(shell = get()) }
    single<RootAccessManager> { get<LibsuRootManager>() }
    single<RootCommandExecutor> { get<LibsuRootManager>() }
}
