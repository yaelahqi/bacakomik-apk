package id.pina.bacakomik.data

data class Manga(
    val slug: String,
    val title: String,
    val cover: String,
    val type: String,
    val theme: String,
    val upCount: String?,
    val url: String,
    val latestChapterTitle: String,
    val latestChapterUrl: String,
    val totalChapters: String
)
