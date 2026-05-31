package id.pina.bacakomik.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.pina.bacakomik.data.repo.LibraryEntry
import id.pina.bacakomik.data.repo.LibraryRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val entries: List<LibraryEntry> = emptyList(),
    val loading: Boolean = true,
)

class LibraryViewModel : ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state

    fun load(ctx: Context) {
        viewModelScope.launch {
            LibraryRepo.observe(ctx).collect { list ->
                _state.update {
                    it.copy(
                        loading = false,
                        entries = list.sortedByDescending { e ->
                            maxOf(e.lastReadAt, e.savedAt)
                        },
                    )
                }
            }
        }
    }
}
