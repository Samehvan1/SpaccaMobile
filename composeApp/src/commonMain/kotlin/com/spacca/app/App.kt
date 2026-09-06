package com.spacca.app

import androidx.compose.runtime.Composable
import com.spacca.app.data.ApiService
import com.spacca.app.data.BranchStore
import com.spacca.app.data.CartStore
import com.spacca.app.data.CatalogRepository
import com.spacca.app.data.EnvironmentStore
import com.spacca.app.data.SessionStore
import com.spacca.app.data.cache.CacheStore
import com.spacca.app.data.cache.PersistentCookiesStorage
import com.spacca.app.data.cache.cacheDirectory
import com.spacca.app.data.createHttpClient
import com.spacca.app.data.createPlatformEngine
import com.spacca.app.data.location.LocationProvider
import com.spacca.app.data.location.LocationStore
import com.spacca.app.ui.navigation.AppNavHost
import com.spacca.app.ui.theme.SpaccaTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.module

val appModule = module {
    single { CacheStore(cacheDirectory()) }
    single { PersistentCookiesStorage(get()) }
    single { SessionStore(get()) }
    single { EnvironmentStore(get()) }
    single { CartStore() }
    single { createHttpClient(createPlatformEngine(), get<PersistentCookiesStorage>()) }
    single { ApiService(get(), get<PersistentCookiesStorage>()) }
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
        // Eagerly create EnvironmentStore so the persisted API environment
        // (VPS/local) is applied to ApiConfig.BASE_URL BEFORE any network
        // request happens. Without this, the first requests would use the
        // default VPS until the user opens the "More" screen.
        koinInject<EnvironmentStore>()
        SpaccaTheme {
            AppNavHost()
        }
    }
}
