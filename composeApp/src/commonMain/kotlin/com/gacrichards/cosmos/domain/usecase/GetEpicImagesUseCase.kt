package com.gacrichards.cosmos.domain.usecase

import com.gacrichards.cosmos.domain.model.EpicImage
import com.gacrichards.cosmos.domain.repository.EpicRepository
import kotlinx.coroutines.flow.Flow

class GetEpicImagesUseCase(private val repository: EpicRepository) {
    operator fun invoke(date: String): Flow<Result<List<EpicImage>>> =
        repository.getImages(date)
}
