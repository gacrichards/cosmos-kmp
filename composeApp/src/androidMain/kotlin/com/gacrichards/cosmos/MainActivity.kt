package com.gacrichards.cosmos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gacrichards.cosmos.data.local.ApodCache
import com.gacrichards.cosmos.data.local.CosmosDatabase
import com.gacrichards.cosmos.data.local.DatabaseDriverFactory
import com.gacrichards.cosmos.data.local.EpicCache
import com.gacrichards.cosmos.data.remote.ApodApiService
import com.gacrichards.cosmos.data.remote.EpicApiService
import com.gacrichards.cosmos.data.remote.createHttpClient
import com.gacrichards.cosmos.data.repository.ApodRepositoryImpl
import com.gacrichards.cosmos.data.repository.EpicRepositoryImpl
import com.gacrichards.cosmos.domain.usecase.GetEpicAvailableDatesUseCase
import com.gacrichards.cosmos.domain.usecase.GetEpicImagesUseCase
import com.gacrichards.cosmos.domain.usecase.GetTodayApodUseCase
import com.gacrichards.cosmos.presentation.epic.EpicViewModel
import com.gacrichards.cosmos.presentation.today.TodayViewModel
import com.gacrichards.cosmos.ui.CosmosApp
import com.gacrichards.cosmos.ui.theme.CosmosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val database = CosmosDatabase(DatabaseDriverFactory(applicationContext).createDriver())
        val httpClient = createHttpClient()
        val apiKey = BuildConfig.NASA_API_KEY

        val apodRepository = ApodRepositoryImpl(
            apiService = ApodApiService(httpClient, apiKey),
            cache = ApodCache(database),
        )
        val epicRepository = EpicRepositoryImpl(
            apiService = EpicApiService(httpClient, apiKey),
            cache = EpicCache(database),
        )

        setContent {
            CosmosTheme {
                val todayViewModel = viewModel {
                    TodayViewModel(GetTodayApodUseCase(apodRepository))
                }
                val epicViewModel = viewModel {
                    EpicViewModel(
                        getImages = GetEpicImagesUseCase(epicRepository),
                        getAvailableDates = GetEpicAvailableDatesUseCase(epicRepository),
                    )
                }
                CosmosApp(
                    todayViewModel = todayViewModel,
                    epicViewModel = epicViewModel,
                )
            }
        }
    }
}
