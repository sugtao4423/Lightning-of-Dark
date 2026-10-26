package sugtao4423.lod.ui.showmedia

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.Regex
import sugtao4423.lod.utils.showToast
import sugtao4423.twitter4j.MediaEntity

data class ShowMediaUiState(
    val pageText: String = "",
    val enableSensorOrientation: Boolean = false,
)

sealed interface ShowMedia {
    val url: String

    data class Image(override val url: String, val thumbnailUrl: String?) : ShowMedia
    data class Video(override val url: String, val repeat: Boolean) : ShowMedia
}

class ShowMediaActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    private val enableImageOrientationSensor = app.prefRepository.isImageOrientationSensor
    private val enableVideoOrientationSensor = app.prefRepository.isVideoOrientationSensor

    private val _uiState = MutableStateFlow(ShowMediaUiState())
    val uiState = _uiState.asStateFlow()

    val showMedia = mutableListOf<ShowMedia>()

    var currentPageIndex = 0
        set(value) {
            field = value
            val pageText = "${value + 1}/${showMedia.size}"
            val enableSensorOrientation = when (showMedia[value]) {
                is ShowMedia.Image -> enableImageOrientationSensor
                is ShowMedia.Video -> enableVideoOrientationSensor
            }
            _uiState.update {
                it.copy(pageText = pageText, enableSensorOrientation = enableSensorOrientation)
            }
        }

    fun setIntentData(mediaEntities: ArrayList<MediaEntity>?, urls: Array<String>?, position: Int) {
        showMedia.clear()
        mediaEntities?.forEach { mediaEntity ->
            when (mediaEntity.type) {
                "photo" -> {
                    val url = mediaEntity.mediaOriginalUrl
                    val thumbnailUrl = mediaEntity.mediaSmallUrl
                    showMedia.add(ShowMedia.Image(url, thumbnailUrl))
                }

                "video", "animated_gif" -> {
                    val info = mediaEntity.videoInfo ?: return@forEach
                    val url = info.variants.maxByOrNull { it.bitrate }?.url ?: return@forEach
                    val repeat = mediaEntity.type == "animated_gif"
                    showMedia.add(ShowMedia.Video(url, repeat))
                }
            }
        }
        urls?.forEach { url ->
            showMedia.add(ShowMedia.Image(url, null))
        }
        currentPageIndex = position
    }

    fun saveCurrentMedia() {
        val url = showMedia[currentPageIndex].url
        val uri = url.toUri()
        val fileName = run {
            val banner = Regex.userBannerUrl.matcher(url)
            when {
                banner.find() -> banner.group(Regex.userBannerUrlFileNameGroup)!! + ".jpg"
                else -> uri.lastPathSegment
                    ?: throw UnsupportedOperationException("Cannot extract file name from URL: $url")
            }
        }
        app.fileDownloader.download(uri, fileName) {
            app.showToast(R.string.param_saved, fileName)
        }
    }

}
