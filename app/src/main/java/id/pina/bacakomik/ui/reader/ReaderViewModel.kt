package id.pina.bacakomik.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.model.ChapterPages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val data: ChapterPages? = null,
)

class ReaderViewModel : ViewModel() {
    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state

    fun load(slug: String) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val r = KomikApi.chapter(slug)
                _state.update { it.copy(loading = false, data = r) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Error") }
            }
        }
    }
}
