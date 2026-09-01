package com.spacca.app

import androidx.compose.runtime.Composable
import com.spacca.app.data.ApiService
import com.spacca.app.data.BranchStore
import com.spacca.app.data.CartStore
import com.spacca.app.data.CatalogRepository
import com.spacca.app.data.SessionStore
import com.spacca.app.data.cache.CacheStore
import com.spacca.app.data.cache.cacheDirectory
import com.spacca.app.data.createHttpClient
import com.spacca.app.data.createPlatformEngine
import com.spacca.app.data.location.LocationProvider
import com.spacca.app.data.location.LocationStore
import com.spacca.app.ui.navigation.AppNavHost
import com.spacca.app.ui.theme.SpaccaTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.module

val appModule = module {
    single { SessionStore() }
    single { CartStore() }
    single { createHttpClient(createPlatformEngine()) }
    single { ApiService(get()) }
    single { CacheStore(cacheDirectory()) }
    single { CatalogRepository(get(), get()) }
    single { LocationProvider() }
    single { LocationStore(get()) }
    single { BranchStore(get(), get()) }
}

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        SpaccaTheme {
            AppNavHost()
        }
    }
}
