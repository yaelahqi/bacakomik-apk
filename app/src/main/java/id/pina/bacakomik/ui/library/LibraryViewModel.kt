package id.pina.bacakomik.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.pina.bacakomik.data.repo.LibraryEntry
import id.pina.bacakomik.data.repo.LibraryRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

data class LibraryUiState(
    val all: List<LibraryEntry> = emptyList(),
    val visible: List<LibraryEntry> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val hasMore: Boolean = false,
)

class LibraryViewModel : ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state
    private var observeJob: Job? = null

    fun load(ctx: Context) {
        if (observeJob != null) return
        observeJob = viewModelScope.launch {
            LibraryRepo.observe(ctx).collect { list ->
                val sorted = list.sortedByDescending { e -> maxOf(e.lastReadAt, e.savedAt) }
                _state.update { cur ->
                    val pageCount = if (cur.visible.isEmpty()) PAGE_SIZE
                                    else maxOf(PAGE_SIZE, cur.visible.size)
                    val take = minOf(pageCount, sorted.size)
                    cur.copy(
                        loading = false,
                        all = sorted,
                        visible = sorted.take(take),
                        hasMore = sorted.size > take,
                    )
                }
            }
        }
    }

    fun loadMore() {
        val cur = _state.value
        if (!cur.hasMore) return
        val next = minOf(cur.visible.size + PAGE_SIZE, cur.all.size)
        _state.update {
            it.copy(visible = cur.all.take(next), hasMore = cur.all.size > next)
        }
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            // tidak ada remote source — jeda kecil supaya indikator sempat muncul
            delay(400)
            val sorted = _state.value.all
            val take = minOf(PAGE_SIZE, sorted.size)
            _state.update {
                it.copy(
                    refreshing = false,
                    visible = sorted.take(take),
                    hasMore = sorted.size > take,
                )
            }
        }
    }
}
