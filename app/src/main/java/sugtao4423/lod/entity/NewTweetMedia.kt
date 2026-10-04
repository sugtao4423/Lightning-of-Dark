package sugtao4423.lod.entity

import android.net.Uri

enum class NewTweetMediaType {
    IMAGE,
    GIF,
    VIDEO,
    UNKNOWN,
}

enum class NewTweetMediaStatus {
    OK,
    UNKNOWN_TYPE,
    TOO_LARGE,
}

data class NewTweetMedia(
    val id: Long,
    val uri: Uri,
    val type: NewTweetMediaType,
    val size: Long,
    val status: NewTweetMediaStatus,
)
