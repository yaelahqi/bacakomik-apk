package id.pina.bacakomik.data

data class MangaDetail(
    val slug: String,
    val title: String,
    val altTitle: String,
    val cover: String,
    val description: String,
    val type: String,
    val status: String,
    val rating: String,
    val author: String,
    val genres: List<String>,
    val theme: String,
    val views: String,
    val chapters: List<ChapterItem>
)

data class ChapterItem(
    val number: String,
    val title: String,
    val url: String,
    val slug: String,
    val date: String
)

data class ChapterRead(
    val chapterSlug: String,
    val title: String,
    val mangaSlug: String,
    val images: List<String>
)
