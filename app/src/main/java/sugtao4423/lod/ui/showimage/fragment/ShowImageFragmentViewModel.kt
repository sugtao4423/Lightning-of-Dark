package sugtao4423.lod.ui.showimage.fragment

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import sugtao4423.lod.R
import sugtao4423.lod.utils.showToast

data class ShowImageUiState(
    val isShowProgressBar: Boolean = true,
)

class ShowImageFragmentViewModel(private val application: Application) :
    AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ShowImageUiState())
    val uiState = _uiState.asStateFlow()

    val requestListener = object : RequestListener<Drawable> {

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            application.showToast(R.string.error_get_image)
            _uiState.update { it.copy(isShowProgressBar = false) }
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            _uiState.update { it.copy(isShowProgressBar = false) }
            return false
        }
    }

}
