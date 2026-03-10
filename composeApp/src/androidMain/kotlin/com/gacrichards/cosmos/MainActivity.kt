package com.gacrichards.cosmos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gacrichards.cosmos.di.appModule
import com.gacrichards.cosmos.ui.CosmosApp
import com.gacrichards.cosmos.ui.theme.CosmosTheme
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger()
                androidContext(applicationContext)
                properties(mapOf("nasa_api_key" to BuildConfig.NASA_API_KEY))
                modules(appModule)
            }
        }

        setContent {
            KoinAndroidContext {
                CosmosTheme {
                    CosmosApp()
                }
            }
        }
    }
}
