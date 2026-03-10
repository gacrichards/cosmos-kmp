package com.gacrichards.cosmos.di

import com.gacrichards.cosmos.data.local.ApodCache
import com.gacrichards.cosmos.data.local.CosmosDatabase
import com.gacrichards.cosmos.data.local.DatabaseDriverFactory
import com.gacrichards.cosmos.data.local.EpicCache
import com.gacrichards.cosmos.data.remote.ApodApiService
import com.gacrichards.cosmos.data.remote.EpicApiService
import com.gacrichards.cosmos.data.remote.createHttpClient
import com.gacrichards.cosmos.data.repository.ApodRepositoryImpl
import com.gacrichards.cosmos.data.repository.EpicRepositoryImpl
import com.gacrichards.cosmos.domain.repository.ApodRepository
import com.gacrichards.cosmos.domain.repository.EpicRepository
import com.gacrichards.cosmos.domain.usecase.GetApodArchiveUseCase
import com.gacrichards.cosmos.domain.usecase.GetApodByDateUseCase
import com.gacrichards.cosmos.domain.usecase.GetEpicAvailableDatesUseCase
import com.gacrichards.cosmos.domain.usecase.GetEpicImagesUseCase
import com.gacrichards.cosmos.domain.usecase.GetTodayApodUseCase
import com.gacrichards.cosmos.presentation.archive.ArchiveViewModel
import com.gacrichards.cosmos.presentation.detail.MediaDetailViewModel
import com.gacrichards.cosmos.presentation.epic.EpicViewModel
import com.gacrichards.cosmos.presentation.today.TodayViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Core
    single { createHttpClient() }
    single { DatabaseDriverFactory(androidContext()) }
    single { CosmosDatabase(get<DatabaseDriverFactory>().createDriver()) }

    // Caches
    single { ApodCache(get()) }
    single { EpicCache(get()) }

    // Services
    single { ApodApiService(get(), getProperty("nasa_api_key")) }
    single { EpicApiService(get(), getProperty("nasa_api_key")) }

    // Repositories
    single<ApodRepository> { ApodRepositoryImpl(get(), get()) }
    single<EpicRepository> { EpicRepositoryImpl(get(), get()) }

    // Use cases
    factory { GetTodayApodUseCase(get()) }
    factory { GetApodArchiveUseCase(get()) }
    factory { GetApodByDateUseCase(get()) }
    factory { GetEpicImagesUseCase(get()) }
    factory { GetEpicAvailableDatesUseCase(get()) }

    // ViewModels
    viewModel { TodayViewModel(get()) }
    viewModel { ArchiveViewModel(get()) }
    viewModel { (date: String) -> MediaDetailViewModel(get(), date) }
    viewModel { EpicViewModel(get(), get()) }
}
