package com.gacrichards.cosmos.domain.usecase

import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow

class GetApodByDateUseCase(private val repository: ApodRepository) {
    operator fun invoke(date: String): Flow<Result<Apod>> =
        repository.getApodByDate(date)
}
