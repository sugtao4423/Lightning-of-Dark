package sugtao4423.lod.swipe_image_viewer

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
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
    lateinit var nonOrigImage: ByteArray

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        url = arguments!!.getString(ImagePagerAdapter.BUNDLE_KEY_URL)!!

        parentLayout = FrameLayout(context!!)
        image = ZoomImageView(context!!)

        progressBar = ProgressBar(context!!, null, android.R.attr.progressBarStyleHorizontal).apply {
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

        val requestListener = object : RequestListener<ByteArray> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<ByteArray>?, isFirstResource: Boolean): Boolean {
                CoroutineScope(Dispatchers.Main).launch {
                    ShowToast(context!!.applicationContext, R.string.error_get_image)
                }
                return false
            }

            override fun onResourceReady(resource: ByteArray?, model: Any?, target: Target<ByteArray>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                CoroutineScope(Dispatchers.Main).launch {
                    nonOrigImage = resource!!
                    val bitmap = BitmapFactory.decodeByteArray(resource, 0, resource.size)
                    image.setImageBitmap(bitmap)
                    progressBar.visibility = View.GONE
                }
                return false
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            Glide.with(this@ImageFragment).`as`(ByteArray::class.java).load(url).listener(requestListener).submit().get()
        }
    }

}