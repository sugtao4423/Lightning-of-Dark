package sugtao4423.lod.swipe_image_viewer

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tenthbit.view.ZoomImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast

class ImageFragment : Fragment() {

    private lateinit var parentLayout: FrameLayout
    private lateinit var image: ZoomImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var url: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        url = requireArguments().getString(ImagePagerAdapter.BUNDLE_KEY_URL)!!

        parentLayout = FrameLayout(requireContext())
        image = ZoomImageView(requireContext())

        progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            scaleY = 1.5f
            visibility = View.VISIBLE
        }

        val imageLayoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        parentLayout.addView(image, imageLayoutParams)

        val barLayoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        barLayoutParams.setMargins(64, 0, 64, 0)
        parentLayout.addView(progressBar, barLayoutParams)

        return parentLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val requestListener = object : RequestListener<Bitmap> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                CoroutineScope(Dispatchers.Main).launch {
                    ShowToast(requireContext().applicationContext, R.string.error_get_image)
                }
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                CoroutineScope(Dispatchers.Main).launch {
                    image.setImageBitmap(resource)
                    progressBar.visibility = View.GONE
                }
                return false
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            Glide.with(this@ImageFragment).asBitmap().load(url).listener(requestListener).submit().get()
        }
    }

}