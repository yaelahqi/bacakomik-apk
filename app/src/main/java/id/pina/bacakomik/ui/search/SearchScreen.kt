package id.pina.bacakomik.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pina.bacakomik.ui.components.ComicCard
import id.pina.bacakomik.ui.components.EmptyBox
import id.pina.bacakomik.ui.components.ErrorBox
import id.pina.bacakomik.ui.components.GridSkeleton
import id.pina.bacakomik.ui.theme.PinaNavy
import id.pina.bacakomik.ui.theme.PinaNavyCard
import id.pina.bacakomik.ui.theme.PinaTextMuted
import id.pina.bacakomik.ui.theme.PinaTextPrimary

@Composable
fun SearchScreen(onOpen: (slug: String) -> Unit) {
    val vm: SearchViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(PinaNavy)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQueryChange,
            placeholder = { Text("Cari komik...", color = PinaTextMuted) },
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = PinaTextMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PinaNavyCard,
                unfocusedContainerColor = PinaNavyCard,
                focusedTextColor = PinaTextPrimary,
                unfocusedTextColor = PinaTextPrimary,
                focusedBorderColor = PinaTextMuted,
                unfocusedBorderColor = PinaTextMuted.copy(alpha = 0.3f),
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )

        when {
            state.loading -> GridSkeleton(itemCount = 9)
            state.error != null -> ErrorBox("Gagal: ${state.error}")
            state.query.isBlank() -> EmptyBox("Mulai mengetik untuk mencari")
            state.items.isEmpty() -> EmptyBox("Tidak ada hasil untuk \"${state.query}\"")
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.items, key = { it.slug }) { item ->
                    ComicCard(item) { onOpen(item.slug) }
                }
            }
        }
    }
}
