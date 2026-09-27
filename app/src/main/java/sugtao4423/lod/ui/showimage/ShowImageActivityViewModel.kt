package sugtao4423.lod.ui.showimage

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.Regex
import sugtao4423.lod.utils.showToast

sealed interface ShowImageEvent {
    data class ShowImageOptionDialog(val dialogItemRes: Int, val openImageUri: Uri) : ShowImageEvent
}

class ShowImageActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val isImageOrientationSensor: Boolean
        get() = app.prefRepository.isImageOrientationSensor

    var imageUrls: Array<String> = arrayOf()
    var imageType = -1
    var currentPageIndex = 0

    private val _events = Channel<ShowImageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun clickImageOptionButton() {
        val imageUrl = imageUrls[currentPageIndex]
        val existsOriginal =
            (imageType != ShowImageActivity.TYPE_BANNER && imageType != ShowImageActivity.TYPE_ICON)
        val listItemRes = if (existsOriginal) R.array.image_option_orig else R.array.image_option
        val openUrl = if (existsOriginal) "$imageUrl:orig" else imageUrl
        _events.trySend(
            ShowImageEvent.ShowImageOptionDialog(listItemRes, openUrl.toUri())
        )
    }

    fun saveCurrentImage() {
        val imageUrl = imageUrls[currentPageIndex]
        when (imageType) {
            ShowImageActivity.TYPE_BANNER -> saveBannerImage(imageUrl)
            ShowImageActivity.TYPE_ICON -> saveIconImage(imageUrl)
            else -> saveTwitterImage(imageUrl)
        }
    }

    private fun saveBannerImage(imageUrl: String) {
        val banner = Regex.userBannerUrl.matcher(imageUrl)
        if (!banner.find()) {
            app.showToast(R.string.url_not_match_pattern_and_dont_save)
            return
        }
        val fileName = banner.group(Regex.userBannerUrlFileNameGroup)!! + ".jpg"
        app.fileDownloader.download(imageUrl.toUri(), fileName) {
            app.showToast(R.string.param_saved, fileName)
        }
    }

    private fun saveIconImage(imageUrl: String) {
        val pattern = Regex.twimgUrl.matcher(imageUrl)
        if (!pattern.find()) {
            app.showToast(R.string.url_not_match_pattern_and_dont_save)
            return
        }
        val fileName = pattern.group(Regex.twimgUrlFileNameGroup)!! +
                pattern.group(Regex.twimgUrlDotExtGroup)!!
        app.fileDownloader.download(imageUrl.toUri(), fileName) {
            app.showToast(R.string.param_saved, fileName)
        }
    }

    private fun saveTwitterImage(imageUrl: String) {
        val originalImageUrl = "$imageUrl:orig"
        val pattern = Regex.twimgUrl.matcher(originalImageUrl)
        if (!pattern.find()) {
            app.showToast(R.string.url_not_match_pattern_and_dont_save)
            return
        }
        val fileName = pattern.group(Regex.twimgUrlFileNameGroup)!! +
                pattern.group(Regex.twimgUrlDotExtGroup)!!.replace(Regex(":orig$"), "")
        app.fileDownloader.download(originalImageUrl.toUri(), fileName) {
            app.showToast(R.string.param_saved_original, fileName)
        }
    }

}
