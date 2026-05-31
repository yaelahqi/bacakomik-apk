package id.pina.bacakomik.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.model.MangaDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val manga: MangaDetail? = null,
    val saved: Boolean = false,
)

class DetailViewModel : ViewModel() {
    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state

    fun load(slug: String) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val m = KomikApi.manga(slug)
                _state.update { it.copy(loading = false, manga = m) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Error") }
            }
        }
    }

    fun setSaved(v: Boolean) { _state.update { it.copy(saved = v) } }
}
