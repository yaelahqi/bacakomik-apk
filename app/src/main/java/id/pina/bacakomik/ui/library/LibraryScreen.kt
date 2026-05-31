package id.pina.bacakomik.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.pina.bacakomik.data.api.KomikApi
import id.pina.bacakomik.ui.components.EmptyBox
import id.pina.bacakomik.ui.components.LoadingBox

@Composable
fun LibraryScreen(onOpen: (slug: String) -> Unit) {
    val vm: LibraryViewModel = viewModel()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.load(ctx) }
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Library",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp),
        )

        when {
            state.loading -> LoadingBox()
            state.entries.isEmpty() -> EmptyBox("Belum ada komik tersimpan.\nTap ikon bookmark di halaman komik.")
            else -> LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.entries, key = { it.slug }) { entry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onOpen(entry.slug) }
                            .padding(8.dp),
                    ) {
                        Box(
                            Modifier
                                .size(width = 56.dp, height = 80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface),
                        ) {
                            AsyncImage(
                                model = KomikApi.imageUrl(entry.cover),
                                contentDescription = entry.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(
                                entry.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                entry.type,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            entry.lastChapterTitle?.let {
                                Text(
                                    "Terakhir: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
