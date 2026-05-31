package id.pina.bacakomik.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.model.ListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val items: List<ListItem> = emptyList(),
    val page: Int = 1,
    val hasNext: Boolean = false,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val type: String = "all",
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(loading = true, error = null, items = emptyList(), page = 1) }
        viewModelScope.launch {
            try {
                val r = KomikApi.list(page = 1, type = _state.value.type)
                _state.update {
                    it.copy(loading = false, items = r.items, page = r.page, hasNext = r.hasNext)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Error") }
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.loadingMore || !s.hasNext) return
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            try {
                val nextPage = s.page + 1
                val r = KomikApi.list(page = nextPage, type = s.type)
                _state.update {
                    it.copy(
                        loadingMore = false,
                        items = it.items + r.items,
                        page = r.page,
                        hasNext = r.hasNext,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMore = false, error = e.message ?: "Error") }
            }
        }
    }

    fun setType(type: String) {
        if (type == _state.value.type) return
        _state.update { it.copy(type = type) }
        refresh()
    }
}
