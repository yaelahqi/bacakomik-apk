package id.pina.bacakomik.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.model.ListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val items: List<ListItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state
    private var job: Job? = null

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
        job?.cancel()
        if (q.isBlank()) {
            _state.update { it.copy(items = emptyList(), loading = false, error = null) }
            return
        }
        job = viewModelScope.launch {
            delay(350)
            _state.update { it.copy(loading = true, error = null) }
            try {
                val r = KomikApi.search(q)
                _state.update { it.copy(loading = false, items = r.items) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Error") }
            }
        }
    }
}
