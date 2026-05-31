package id.pina.bacakomik.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.pina.bacakomik.data.model.ListItem
import id.pina.bacakomik.data.repo.HistoryEntry
import id.pina.bacakomik.data.repo.HistoryRepo
import id.pina.bacakomik.data.repo.LibraryEntry
import id.pina.bacakomik.data.repo.LibraryRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

enum class Tab { FAVORITES, HISTORY }

data class LibraryUiState(
    val tab: Tab = Tab.FAVORITES,
    val all: List<LibraryEntry> = emptyList(),
    val visible: List<LibraryEntry> = emptyList(),
    val historyItems: List<HistoryEntry> = emptyList(),
    val visibleHistory: List<HistoryEntry> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val hasMore: Boolean = false,
    val hasMoreHistory: Boolean = false,
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
        viewModelScope.launch {
            HistoryRepo.observe(ctx).collect { list ->
                val sorted = list.sortedByDescending { it.readAt }
                _state.update { cur ->
                    val take = minOf(PAGE_SIZE, sorted.size)
                    cur.copy(
                        historyItems = sorted,
                        visibleHistory = sorted.take(take),
                        hasMoreHistory = sorted.size > take,
                    )
                }
            }
        }
    }

    fun setTab(tab: Tab) {
        _state.update { it.copy(tab = tab) }
    }

    fun loadMore() {
        val cur = _state.value
        if (cur.tab == Tab.FAVORITES) {
            if (!cur.hasMore) return
            val next = minOf(cur.visible.size + PAGE_SIZE, cur.all.size)
            _state.update {
                it.copy(visible = cur.all.take(next), hasMore = cur.all.size > next)
            }
        } else {
            if (!cur.hasMoreHistory) return
            val next = minOf(cur.visibleHistory.size + PAGE_SIZE, cur.historyItems.size)
            _state.update {
                it.copy(visibleHistory = cur.historyItems.take(next), hasMoreHistory = cur.historyItems.size > next)
            }
        }
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            delay(400)
            val cur = _state.value
            if (cur.tab == Tab.FAVORITES) {
                val sorted = cur.all
                val take = minOf(PAGE_SIZE, sorted.size)
                _state.update {
                    it.copy(refreshing = false, visible = sorted.take(take), hasMore = sorted.size > take)
                }
            } else {
                val sorted = cur.historyItems
                val take = minOf(PAGE_SIZE, sorted.size)
                _state.update {
                    it.copy(refreshing = false, visibleHistory = sorted.take(take), hasMoreHistory = sorted.size > take)
                }
            }
        }
    }

    fun clearHistory(ctx: Context) {
        viewModelScope.launch {
            HistoryRepo.clear(ctx)
        }
    }

    fun removeFromHistory(ctx: Context, entry: HistoryEntry) {
        viewModelScope.launch {
            HistoryRepo.removeHistory(ctx, entry.slug, entry.chapterSlug)
        }
    }

    fun removeFromFavorites(ctx: Context, slug: String) {
        viewModelScope.launch {
            val item = LibraryRepo.all(ctx).find { it.slug == slug }
            if (item != null) {
                LibraryRepo.toggle(
                    ctx, ListItem(
                        slug = item.slug,
                        title = item.title,
                        cover = item.cover ?: "",
                        type = item.type,
                    )
                )
            }
        }
    }
}
