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

sealed interface VideoInfo : java.io.Serializable {
    val aspectRatioWidth: Int
    val aspectRatioHeight: Int
    val variants: List<VideoVariant>

    data class Gif(
        override val aspectRatioWidth: Int,
        override val aspectRatioHeight: Int,
        override val variants: List<VideoVariant>,
    ) : VideoInfo

    data class Video(
        override val aspectRatioWidth: Int,
        override val aspectRatioHeight: Int,
        val durationMillis: Long,
        override val variants: List<VideoVariant>,
    ) : VideoInfo
}

sealed interface MediaEntity : Indices, java.io.Serializable {
    val id: Long
    val url: String
    val mediaUrl: String
    val expandedUrl: String
    val displayUrl: String
    val sizes: Map<MediaSize, Size>
    val extAltText: String?
    override val start: Int
    override val end: Int

    val mediaThumbnailUrl: String get() = "${mediaUrl}?name=thumb"
    val mediaSmallUrl: String get() = "${mediaUrl}?name=small"
    val mediaMediumUrl: String get() = "${mediaUrl}?name=medium"
    val mediaLargeUrl: String get() = "${mediaUrl}?name=large"
    val mediaOriginalUrl: String get() = "${mediaUrl}?name=orig"

    sealed interface HasVideo : MediaEntity {
        val videoInfo: VideoInfo
    }

    data class Image(
        override val id: Long,
        override val url: String,
        override val mediaUrl: String,
        override val expandedUrl: String,
        override val displayUrl: String,
        override val sizes: Map<MediaSize, Size>,
        override val extAltText: String?,
        override val start: Int,
        override val end: Int,
    ) : MediaEntity

    data class Gif(
        override val id: Long,
        override val url: String,
        override val mediaUrl: String,
        override val expandedUrl: String,
        override val displayUrl: String,
        override val sizes: Map<MediaSize, Size>,
        override val extAltText: String?,
        override val videoInfo: VideoInfo.Gif,
        override val start: Int,
        override val end: Int,
    ) : MediaEntity, HasVideo

    data class Video(
        override val id: Long,
        override val url: String,
        override val mediaUrl: String,
        override val expandedUrl: String,
        override val displayUrl: String,
        override val sizes: Map<MediaSize, Size>,
        override val extAltText: String?,
        override val videoInfo: VideoInfo.Video,
        override val start: Int,
        override val end: Int,
    ) : MediaEntity, HasVideo

}
