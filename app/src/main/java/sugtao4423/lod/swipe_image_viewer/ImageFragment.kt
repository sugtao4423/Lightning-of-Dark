package sugtao4423.lod.swipe_image_viewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import com.tenthbit.view.ZoomImageView
import sugtao4423.lod.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null || context == null) {
            return
        }
        url = arguments!!.getString(ImagePagerAdapter.BUNDLE_KEY_URL) ?: return

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        object : AsyncTask<Unit, Unit, Bitmap?>() {

            override fun doInBackground(vararg params: Unit?): Bitmap? {
                return try {
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
                    return bitmap
                } catch (e: IOException) {
                    null
                }
            }

            override fun onPostExecute(result: Bitmap?) {
                if (result != null) {
                    progressBar.visibility = View.GONE
                    image.setImageBitmap(result)
                } else {
                    Toast.makeText(activity, R.string.error_get_image, Toast.LENGTH_LONG).show()
                }
            }
        }.execute()

        return parentLayout
    }

}