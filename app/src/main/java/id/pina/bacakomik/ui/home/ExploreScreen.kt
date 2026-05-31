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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.ui.components.ComicCard
import id.pina.bacakomik.ui.components.ErrorBox
import id.pina.bacakomik.ui.components.GridSkeleton
import id.pina.bacakomik.ui.theme.PinaNavy
import id.pina.bacakomik.ui.theme.PinaNavyCard
import id.pina.bacakomik.ui.theme.PinaRed
import id.pina.bacakomik.ui.theme.PinaTextPrimary
import id.pina.bacakomik.ui.theme.PinaTextSecondary
import id.pina.bacakomik.ui.theme.PinaTextMuted

/** Explore tab: type filter chips + full manga grid. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(onOpen: (slug: String) -> Unit) {
    var selectedType by remember { mutableStateOf("all") }
    var selectedPage by remember { mutableStateOf(1) }
    var items by remember { mutableStateOf(emptyList<id.pina.bacakomik.data.model.ListItem>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var hasNext by remember { mutableStateOf(false) }

    LaunchedEffect(selectedType, selectedPage) {
        loading = true; error = null
        try {
            val r = KomikApi.list(page = selectedPage, type = selectedType)
            items = if (selectedPage == 1) r.items else items + r.items
            hasNext = r.hasNext
        } catch (e: Exception) {
            error = e.message ?: "Error"
        } finally {
            loading = false
        }
    }

    val types = listOf(
        "all" to "Semua",
        "manga" to "Manga",
        "manhwa" to "Manhwa",
        "manhua" to "Manhua",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(PinaNavy),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // Title
        item {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                color = PinaTextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }

        // Type chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(types) { (key, label) ->
                    val selected = selectedType == key
                    FilterChip(
                        selected = selected,
                        onClick = { selectedType = key; selectedPage = 1 },
                        label = {
                            Text(
                                text = label,
                                color = if (selected) Color.White else PinaTextSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = PinaNavyCard,
                            selectedContainerColor = PinaRed,
                        ),
                    )
                }
            }
        }

        // Genre quick access — row of small icon pills
        item {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Jelajahi Genre",
                style = MaterialTheme.typography.titleMedium,
                color = PinaTextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val genres = listOf("Action", "Romance", "Fantasy", "Isekai", "Comedy", "Adventure", "Drama", "Martial Arts")
                items(genres) { genre ->
                    Column(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PinaNavyCard)
                            .clickable { onOpen("genre/$genre") }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(text = genreEmoji(genre), fontSize = 24.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.labelSmall,
                            color = PinaTextSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // Grid
        item {
            Spacer(Modifier.height(12.dp))
        }

        if (loading && items.isEmpty()) {
            item { GridSkeleton(itemCount = 9) }
        } else if (error != null && items.isEmpty()) {
            item { ErrorBox("Gagal memuat: $error") }
        } else {
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height((items.size / 3 * 230 + 100).coerceAtMost(2000).dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    userScrollEnabled = false,
                ) {
                    itemsIndexed(items, key = { _, it -> it.slug }) { _, item ->
                        ComicCard(item) { onOpen(item.slug) }
                    }
                }
            }
        }

        // Load more
        if (hasNext) {
            item {
                LaunchedEffect(Unit) { selectedPage++ }
                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PinaRed, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

private fun genreEmoji(genre: String): String = when (genre) {
    "Action" -> "\uD83D\uDCA5"
    "Romance" -> "\u2764\uFE0F"
    "Fantasy" -> "\uD83E\uDDD9"
    "Isekai" -> "\uD83C\uDF0D"
    "Comedy" -> "\uD83D\uDE02"
    "Adventure" -> "\uD83C\uDFD4\uFE0F"
    "Drama" -> "\uD83C\uDFAD"
    "Martial Arts" -> "\uD83E\uDD4A"
    else -> "\uD83D\uDCD6"
}
