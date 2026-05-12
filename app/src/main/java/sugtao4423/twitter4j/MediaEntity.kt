package sugtao4423.twitter4j

enum class MediaResize {
    FIT, CROP
}

enum class MediaSize {
    LARGE, MEDIUM, SMALL, THUMB
}

data class Size(
    val width: Int,
    val height: Int,
    val resize: MediaResize,
) : java.io.Serializable

data class VideoVariant(
    val bitrate: Int,
    val contentType: String,
    val url: String,
) : java.io.Serializable

data class VideoInfo(
    val aspectRatioWidth: Int,
    val aspectRatioHeight: Int,
    val durationMillis: Long?,
    val variants: List<VideoVariant>,
) : java.io.Serializable

data class MediaEntity(
    val id: Long,
    val url: String,
    val mediaUrl: String,
    val expandedUrl: String,
    val displayUrl: String,
    val sizes: Map<MediaSize, Size>,
    val type: String,
    val extAltText: String?,
    val videoInfo: VideoInfo?,
    override val start: Int,
    override val end: Int,
) : Indices, java.io.Serializable
