package com.gacrichards.cosmos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gacrichards.cosmos.data.remote.ApodApiService
import com.gacrichards.cosmos.data.remote.createHttpClient
import com.gacrichards.cosmos.data.repository.ApodRepositoryImpl
import com.gacrichards.cosmos.domain.usecase.GetTodayApodUseCase
import com.gacrichards.cosmos.presentation.today.TodayViewModel
import com.gacrichards.cosmos.ui.theme.CosmosTheme
import com.gacrichards.cosmos.ui.today.TodayScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CosmosTheme {
                val viewModel = viewModel {
                    val apiService = ApodApiService(
                        client = createHttpClient(),
                        apiKey = BuildConfig.NASA_API_KEY,
                    )
                    TodayViewModel(GetTodayApodUseCase(ApodRepositoryImpl(apiService)))
                }
                TodayScreen(viewModel = viewModel)
            }
        }
    }
}
