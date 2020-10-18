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
import com.tenthbit.view.ZoomImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

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

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val connection = (URL(url).openConnection() as HttpsURLConnection).apply {
                        doInput = true
                        connect()
                    }
                    val inputStream = connection.inputStream
                    val bout = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var len = inputStream.read(buffer)
                    while (len > 0) {
                        bout.write(buffer, 0, len)
                        len = inputStream.read(buffer)
                    }
                    nonOrigImage = bout.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(nonOrigImage, 0, nonOrigImage.size)
                    inputStream.close()
                    bout.close()
                    connection.disconnect()
                    bitmap
                } catch (e: IOException) {
                    null
                }
            }
            if (result != null) {
                progressBar.visibility = View.GONE
                image.setImageBitmap(result)
            } else {
                ShowToast(context!!.applicationContext, R.string.error_get_image)
            }
        }
    }

}