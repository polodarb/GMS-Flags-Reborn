package ua.polodarb.gmsflags.data.network.impl.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.polodarb.gmsflags.data.network.APP_SIGNATURE_SHA256_QUALIFIER_NAME
import ua.polodarb.gmsflags.data.network.impl.core.HttpClientFactory
import ua.polodarb.gmsflags.data.network.impl.publicapi.KtorGmsFlagsPublicDataSource
import ua.polodarb.gmsflags.data.network.publicapi.GmsFlagsPublicDataSource

val dataNetworkModule = module {
    single<HttpClient> {
        val chuckerInterceptor = ChuckerInterceptor.Builder(androidContext()).build()
        HttpClientFactory.create(
            environment = get(),
            engine = OkHttp.create {
                addInterceptor(chuckerInterceptor)
            },
            appSignatureSha256 = get(qualifier = named(APP_SIGNATURE_SHA256_QUALIFIER_NAME)),
        )
    }
    single<GmsFlagsPublicDataSource> {
        KtorGmsFlagsPublicDataSource(httpClient = get(), environment = get())
    }
}
