package sugtao4423.twitter4j

interface Indices {
    val start: Int
    val end: Int
}

data class SymbolEntity(
    override val start: Int,
    override val end: Int,
) : Indices, java.io.Serializable

data class HashtagEntity(
    val text: String,
    override val start: Int,
    override val end: Int,
) : Indices, java.io.Serializable

data class UrlEntity(
    val url: String,
    val expandedUrl: String?,
    val displayUrl: String,
    override val start: Int,
    override val end: Int,
) : Indices, java.io.Serializable

data class UserMentionEntity(
    val id: Long,
    val name: String,
    val screenName: String,
    override val start: Int,
    override val end: Int,
) : Indices, java.io.Serializable

data class QuotedStatusPermalinkEntity(
    val url: String,
    val expandedUrl: String,
    val displayUrl: String,
) : java.io.Serializable
