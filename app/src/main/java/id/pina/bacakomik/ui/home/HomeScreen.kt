package id.pina.bacakomik.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.data.model.ListItem
import id.pina.bacakomik.data.repo.HistoryRepo
import id.pina.bacakomik.ui.components.ComicCard
import id.pina.bacakomik.ui.components.ErrorBox
import id.pina.bacakomik.ui.components.GridSkeleton
import id.pina.bacakomik.ui.theme.PinaNavy
import id.pina.bacakomik.ui.theme.PinaNavyCard
import id.pina.bacakomik.ui.theme.PinaRed
import id.pina.bacakomik.ui.theme.PinaRedDeep
import id.pina.bacakomik.ui.theme.PinaTextPrimary
import id.pina.bacakomik.ui.theme.PinaTextSecondary
import id.pina.bacakomik.ui.theme.PinaTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpen: (slug: String) -> Unit) {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()
    val context = LocalContext.current

    val continueReading by produceState<id.pina.bacakomik.data.repo.HistoryEntry?>(initialValue = null) {
        runCatching { HistoryRepo.observe(context).collect { list ->
            value = list.maxByOrNull { it.readAt }
        } }
    }

    PullToRefreshBox(
        isRefreshing = state.loading,
        onRefresh = { vm.refresh() },
        state = pullState,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(PinaNavy),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // Greeting
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(
                        text = greetingMessage(),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                        color = PinaTextPrimary,
                    )
                    Text(
                        text = "Selamat datang di Pina Komik",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PinaTextSecondary,
                        modifier = Modifier.paddingFromBaseline(top = 6.dp),
                    )
                }
            }

            // Search bar
            item {
                SearchBarPill(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    onClick = { onOpen("__search") },
                )
            }

            // Continue reading hero
            if (continueReading != null) {
                item {
                    Spacer(Modifier.height(12.dp))
                    HeroContinueReading(
                        title = continueReading!!.title,
                        chapter = continueReading!!.chapterTitle ?: "",
                        cover = continueReading!!.cover,
                        onClick = { onOpen(continueReading!!.slug) },
                    )
                }
            }

            // Komik Populer
            item {
                SectionHeader(
                    title = "Komik Populer",
                    subtitle = "Komik yang sering dibaca pengguna",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }

            if (state.loading && state.items.isEmpty()) {
                item { GridSkeleton(itemCount = 6) }
            } else if (state.error != null && state.items.isEmpty()) {
                item { ErrorBox("Gagal memuat: ${state.error}") }
            } else {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(280.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        userScrollEnabled = false,
                    ) {
                        itemsIndexed(state.items.take(6), key = { _, it -> it.slug }) { _, item ->
                            ComicCard(item) { onOpen(item.slug) }
                        }
                    }
                }
            }

            // Update Terbaru
            item {
                SectionHeader(
                    title = "Update Terbaru",
                    subtitle = "Jangan ketinggalan chapter terbaru",
                    actionLabel = "Semua",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }

            val latestItems = state.items.drop(6)
            items(latestItems.size, key = { latestItems[it].slug }) { idx ->
                LatestUpdateCard(latestItems[idx]) { onOpen(latestItems[idx].slug) }
            }

            if (state.loadingMore) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PinaRed, modifier = Modifier.size(32.dp))
                    }
                }
            }

            if (state.items.isNotEmpty() && state.hasNext) {
                item {
                    LaunchedEffect(Unit) { vm.loadMore() }
                }
            }
        }
    }
}

@Composable
private fun SearchBarPill(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PinaNavyCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = PinaTextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("Cari disini", color = PinaTextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HeroContinueReading(title: String, chapter: String, cover: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(PinaRed, PinaRedDeep)))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!cover.isNullOrBlank()) {
            AsyncImage(
                model = KomikApi.imageUrl(cover),
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(text = chapter, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        }
        Text(
            text = "\u2192",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun LatestUpdateCard(item: ListItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PinaNavyCard)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = KomikApi.imageUrl(item.cover),
            contentDescription = null,
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = PinaTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.type.isNotBlank()) {
                    Text("\u25CF", fontSize = 6.sp, color = PinaRed)
                    Spacer(Modifier.width(4.dp))
                    Text(item.type, style = MaterialTheme.typography.bodySmall, color = PinaTextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.latestChapterTitle ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(color = PinaRed, fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String = "",
    actionLabel: String = "",
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = PinaTextPrimary, modifier = Modifier.weight(1f))
            if (actionLabel.isNotBlank() && onAction != null) {
                Text(
                    text = "$actionLabel >",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PinaRed,
                    modifier = Modifier.clickable(onClick = onAction),
                )
            }
        }
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PinaTextSecondary,
                modifier = Modifier.paddingFromBaseline(top = 4.dp),
            )
        }
    }
}

private fun greetingMessage(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Selamat pagi"
        hour < 18 -> "Selamat siang"
        else -> "Selamat malam"
    }
}
