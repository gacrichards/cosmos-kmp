package com.gacrichards.cosmos.presentation.today

sealed class TodayEvent {
    data object Retry : TodayEvent()
}
