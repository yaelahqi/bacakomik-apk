package id.pina.bacakomik.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pina.bacakomik.ui.components.ComicCard
import id.pina.bacakomik.ui.components.ErrorBox
import id.pina.bacakomik.ui.components.GridSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpen: (slug: String) -> Unit) {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Pina Komik",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp),
        )

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("all" to "Semua", "manga" to "Manga", "manhwa" to "Manhwa", "manhua" to "Manhua")
                .forEach { (key, label) ->
                    FilterChip(
                        selected = state.type == key,
                        onClick = { vm.setType(key) },
                        label = { Text(label) },
                    )
                }
        }

        when {
            state.loading && state.items.isEmpty() -> GridSkeleton(itemCount = 9)
            state.error != null && state.items.isEmpty() ->
                ErrorBox("Gagal memuat: ${state.error}")
            else -> {
                PullToRefreshBox(
                    isRefreshing = state.loading,
                    onRefresh = { vm.refresh() },
                    state = pullState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val grid = rememberLazyGridState()
                    val nearBottom by remember {
                        derivedStateOf {
                            val last = grid.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            last >= state.items.size - 4
                        }
                    }
                    LaunchedEffect(nearBottom, state.items.size, state.hasNext) {
                        if (nearBottom && state.hasNext && !state.loadingMore) vm.loadMore()
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = grid,
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(state.items, key = { _, it -> it.slug }) { _, item ->
                            ComicCard(item) { onOpen(item.slug) }
                        }
                        if (state.loadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
