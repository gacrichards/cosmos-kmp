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
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun startKoin(nasaApiKey: String) {
    startKoin {
        modules(
            module {
                single { createHttpClient() }
                single { DatabaseDriverFactory() }
                single { CosmosDatabase(get<DatabaseDriverFactory>().createDriver()) }

                single { ApodCache(get()) }
                single { EpicCache(get()) }

                single { ApodApiService(get(), nasaApiKey) }
                single { EpicApiService(get(), nasaApiKey) }

                single<ApodRepository> { ApodRepositoryImpl(get(), get()) }
                single<EpicRepository> { EpicRepositoryImpl(get(), get()) }

                factory { GetTodayApodUseCase(get()) }
                factory { GetApodArchiveUseCase(get()) }
                factory { GetApodByDateUseCase(get()) }
                factory { GetEpicImagesUseCase(get()) }
                factory { GetEpicAvailableDatesUseCase(get()) }

                factory { TodayViewModel(get()) }
                factory { ArchiveViewModel(get()) }
                factory { EpicViewModel(get(), get()) }
                factory { (date: String) -> MediaDetailViewModel(get(), date) }
            }
        )
    }
}
