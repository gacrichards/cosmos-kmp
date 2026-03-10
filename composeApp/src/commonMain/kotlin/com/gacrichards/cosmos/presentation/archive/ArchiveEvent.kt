package com.gacrichards.cosmos.presentation.archive

sealed class ArchiveEvent {
    data object Retry : ArchiveEvent()
    data object LoadMore : ArchiveEvent()
}
