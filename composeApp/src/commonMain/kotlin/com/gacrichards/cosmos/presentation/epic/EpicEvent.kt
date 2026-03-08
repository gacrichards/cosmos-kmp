package com.gacrichards.cosmos.presentation.epic

sealed class EpicEvent {
    data object NextDate : EpicEvent()
    data object PreviousDate : EpicEvent()
    data object Retry : EpicEvent()
}
