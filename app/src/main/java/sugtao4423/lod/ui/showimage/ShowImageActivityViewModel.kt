package sugtao4423.lod.ui.showimage

import android.app.Application
import android.net.Uri
import androidx.annotation.ArrayRes
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.Regex
import sugtao4423.lod.utils.showToast

class ShowImageActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val isImageOrientationSensor: Boolean
        get() = app.prefRepository.isImageOrientationSensor

    var imageUrls: Array<String> = arrayOf()
    var imageType = -1
    var currentPageIndex = 0

    data class ImageOptionDialogData(
        @ArrayRes val dialogItemRes: Int,
        val openImageUri: Uri,
    )

    private val _showImageOptionDialog = LiveEvent<ImageOptionDialogData>()
    val showImageOptionDialog: LiveData<ImageOptionDialogData> = _showImageOptionDialog

    fun clickImageOptionButton() {
        val imageUrl = imageUrls[currentPageIndex]
        val existsOriginal =
            (imageType != ShowImageActivity.TYPE_BANNER && imageType != ShowImageActivity.TYPE_ICON)
        val listItemRes = if (existsOriginal) R.array.image_option_orig else R.array.image_option
        val openUrl = if (existsOriginal) "$imageUrl:orig" else imageUrl
        val data = ImageOptionDialogData(listItemRes, openUrl.toUri())
        _showImageOptionDialog.value = data
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
