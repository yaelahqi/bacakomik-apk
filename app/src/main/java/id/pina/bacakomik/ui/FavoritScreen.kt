package id.pina.bacakomik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import id.pina.bacakomik.PinaGray
import id.pina.bacakomik.PinaNavy
import id.pina.bacakomik.PinaRed
import id.pina.bacakomik.data.FavoriteItem
import id.pina.bacakomik.data.LocalStore

@Composable
fun FavoritScreen(onMangaClick: (String) -> Unit) {
    val ctx = LocalContext.current
    val store = remember { LocalStore(ctx) }
    var favs by remember { mutableStateOf(store.listFavorites()) }

    // Refresh when coming back
    LaunchedEffect(Unit) {
        favs = store.listFavorites()
    }

    Column(modifier = Modifier.fillMaxSize().background(PinaNavy)) {
        Text(
            "📚 Favorit (${favs.size})",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        if (favs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada favorit.", color = PinaGray, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text("Tap \uD83D\uDD16 di detail komik untuk bookmark.", color = PinaGray, fontSize = 13.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(favs, key = { it.slug }) { fav ->
                    // Reuse PosterCard with a manga-like wrapper
                    PosterCard(
                        manga = id.pina.bacakomik.data.Manga(
                            slug = fav.slug,
                            title = fav.title,
                            cover = fav.cover,
                            type = fav.type,
                            theme = fav.theme,
                            upCount = "",
                            url = "",
                            latestChapterTitle = fav.latestChapter,
                            latestChapterUrl = "",
                            totalChapters = ""
                        ),
                        onClick = { onMangaClick(fav.slug) }
                    )
                }
            }
        }
    }
}
