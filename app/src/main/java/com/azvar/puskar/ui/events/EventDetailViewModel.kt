package com.azvar.puskar.ui.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.azvar.puskar.data.repository.EventRepository
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.domain.model.nowInEventZone
import com.azvar.puskar.ui.navigation.EventDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

sealed interface EventDetailUiState {
    data object Loading : EventDetailUiState
    data object NotFound : EventDetailUiState
    data class Success(val event: CareerEvent, val isPast: Boolean) : EventDetailUiState
}

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EventRepository,
    private val clock: Clock,
) : ViewModel() {

    private val eventId = savedStateHandle.toRoute<EventDetailDestination>().eventId

    val uiState: StateFlow<EventDetailUiState> = repository.observeEvent(eventId)
        .map { event ->
            if (event == null) EventDetailUiState.NotFound
            else EventDetailUiState.Success(event, event.isPast(clock.nowInEventZone()))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventDetailUiState.Loading)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onReminderClick() {
        viewModelScope.launch {
            val enabled = repository.toggleReminder(eventId)
            _message.value = if (enabled) EventsViewModel.REMINDER_ON else EventsViewModel.REMINDER_OFF
        }
    }

    fun onNotificationPermissionDenied() {
        _message.value = EventsViewModel.PERMISSION_DENIED
    }

    fun onMessageShown() {
        _message.value = null
    }
}
