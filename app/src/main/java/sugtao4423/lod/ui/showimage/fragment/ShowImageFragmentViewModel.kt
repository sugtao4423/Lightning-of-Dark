package sugtao4423.lod.ui.showimage.fragment

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast

class ShowImageFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val isShowProgressBar = ObservableField(true)

    var imageUrl: String? = null

    val requestListener = object : RequestListener<Drawable> {

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            ShowToast(app, R.string.error_get_image)
            isShowProgressBar.set(false)
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            isShowProgressBar.set(false)
            return false
        }
    }

}
