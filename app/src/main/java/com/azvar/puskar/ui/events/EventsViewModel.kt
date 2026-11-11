package com.azvar.puskar.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azvar.puskar.data.repository.EventRepository
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.domain.model.nowInEventZone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

enum class EventModeFilter(val label: String) { ALL("Semua"), ONLINE("Online"), OFFLINE("Offline") }

data class EventsUiState(
    val upcoming: List<CareerEvent> = emptyList(),
    val past: List<CareerEvent> = emptyList(),
    val modeFilter: EventModeFilter = EventModeFilter.ALL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository,
    private val clock: Clock,
) : ViewModel() {

    private val modeFilter = MutableStateFlow(EventModeFilter.ALL)
    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<EventsUiState> = combine(
        repository.observeEvents(),
        modeFilter,
        transient,
    ) { events, mode, t ->
        val now = clock.nowInEventZone()
        val filtered = events.filter {
            when (mode) {
                EventModeFilter.ALL -> true
                EventModeFilter.ONLINE -> it.isOnline
                EventModeFilter.OFFLINE -> !it.isOnline
            }
        }
        val (past, upcoming) = filtered.partition { it.isPast(now) }
        EventsUiState(
            upcoming = upcoming,
            // Most recent past events first.
            past = past.sortedByDescending { it.startAt },
            modeFilter = mode,
            isLoading = false,
            isRefreshing = t.isRefreshing,
            message = t.message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventsUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true) }
            val result = repository.refresh()
            transient.update {
                it.copy(isRefreshing = false, message = if (result.isFailure) "Gagal memuat event terbaru" else it.message)
            }
        }
    }

    fun onModeSelected(mode: EventModeFilter) {
        modeFilter.value = mode
    }

    fun onReminderClick(eventId: String) {
        viewModelScope.launch {
            val enabled = repository.toggleReminder(eventId)
            showMessage(if (enabled) REMINDER_ON else REMINDER_OFF)
        }
    }

    fun onNotificationPermissionDenied() = showMessage(PERMISSION_DENIED)

    fun onMessageShown() = transient.update { it.copy(message = null) }

    private fun showMessage(text: String) = transient.update { it.copy(message = text) }

    private data class Transient(val isRefreshing: Boolean = false, val message: String? = null)

    companion object {
        const val REMINDER_ON = "Pengingat aktif: notifikasi muncul 1 jam sebelum acara"
        const val REMINDER_OFF = "Pengingat dimatikan"
        const val PERMISSION_DENIED = "Izin notifikasi ditolak, jadi pengingat tidak akan muncul"
    }
}
