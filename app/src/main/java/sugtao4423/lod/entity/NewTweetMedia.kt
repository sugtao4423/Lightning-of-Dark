package sugtao4423.lod.entity

import android.net.Uri

enum class NewTweetMediaType {
    IMAGE,
    GIF,
    VIDEO,
    UNKNOWN,
}

data class Resolution(val width: Int, val height: Int)

enum class NewTweetMediaStatus {
    OK,
    UNKNOWN_TYPE,
    ALL_TOO_LARGE,
    FILE_TOO_LARGE,
    RESOLUTION_TOO_LARGE;

    val isFileTooLarge: Boolean
        get() = this == ALL_TOO_LARGE || this == FILE_TOO_LARGE
    val isResolutionTooLarge: Boolean
        get() = this == ALL_TOO_LARGE || this == RESOLUTION_TOO_LARGE
}

data class NewTweetMedia(
    val id: Long,
    val uri: Uri,
    val type: NewTweetMediaType,
    val size: Long,
    val resolution: Resolution?,
    val status: NewTweetMediaStatus,
)
