package com.gacrichards.cosmos.domain.usecase

import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow

class GetApodArchiveUseCase(private val repository: ApodRepository) {
    operator fun invoke(startDate: String, endDate: String): Flow<Result<List<Apod>>> =
        repository.getApodArchive(startDate, endDate)
}
