package sugtao4423.lod.swipe_image_viewer

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.Window
import android.widget.EditText
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.show_image_pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sugtao4423.lod.ChromeIntent
import sugtao4423.lod.LoDBaseActivity
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.utils.Regex
import sugtao4423.support.progressdialog.ProgressDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageFragmentActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_URLS = "urls"
        const val INTENT_EXTRA_KEY_POSITION = "position"

        const val TYPE_ICON = 0
        const val TYPE_BANNER = 1
    }

    private lateinit var adapter: ImagePagerAdapter
    private lateinit var urls: Array<String>
    private var type = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.show_image_pager)
        if (app.getOptions().isImageOrientationSensor) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        urls = intent.getStringArrayExtra(INTENT_EXTRA_KEY_URLS)
        type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1)
        val pos = intent.getIntExtra(INTENT_EXTRA_KEY_POSITION, 0)
        adapter = ImagePagerAdapter(supportFragmentManager, urls)

        showImagePager.also {
            it.adapter = adapter
            it.offscreenPageLimit = urls.size - 1
            it.currentItem = pos
        }

        showImagePagerTabStrip.apply {
            tabIndicatorColor = ContextCompat.getColor(applicationContext, R.color.pagerTabText)
            drawFullUnderline = true
        }
    }

    fun clickImageOption(@Suppress("UNUSED_PARAMETER") v: View) {
        val imageUrl = urls[showImagePager.currentItem]
        val existsOriginal = (type != TYPE_BANNER && type != TYPE_ICON)
        val listItemRes = if (existsOriginal) R.array.image_option_orig else R.array.image_option
        AlertDialog.Builder(this).apply {
            setItems(listItemRes) { _, which ->
                when (which) {
                    0 -> {
                        val openUrl = imageUrl + (if (existsOriginal) ":orig" else "")
                        ChromeIntent(this@ImageFragmentActivity, Uri.parse(openUrl))
                    }
                    1 -> saveImage(imageUrl)
                }
            }
            show()
        }
    }

    private fun saveImage(imageUrl: String) {
        if (!hasWriteExternalStoragePermission()) {
            requestWriteExternalStoragePermission()
            return
        }
        if (type == TYPE_BANNER) {
            val banner = Regex.userBannerUrl.matcher(imageUrl)
            if (!banner.find()) {
                ShowToast(applicationContext, R.string.url_not_match_pattern_and_dont_save)
                return
            }
            val bannerImg = (adapter.getItem(showImagePager.currentItem) as ImageFragment).nonOrigImage
            save(banner.group(Regex.userBannerUrlFileNameGroup), ".jpg", bannerImg, false)
            return
        }

        val imgUrl = imageUrl + (if (type == TYPE_ICON) "" else ":orig")

        val pattern = Regex.twimgUrl.matcher(imgUrl)
        if (!pattern.find()) {
            ShowToast(applicationContext, R.string.url_not_match_pattern_and_dont_save)
            return
        }

        val progressDialog = ProgressDialog(this@ImageFragmentActivity).apply {
            setMessage(getString(R.string.loading))
            isIndeterminate = false
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(true)
            show()
        }
        val requestListener = object : RequestListener<ByteArray> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<ByteArray>?, isFirstResource: Boolean): Boolean {
                CoroutineScope(Dispatchers.Main).launch {
                    progressDialog.dismiss()
                    ShowToast(applicationContext, R.string.error_get_original_image)
                }
                return false
            }

            override fun onResourceReady(resource: ByteArray?, model: Any?, target: Target<ByteArray>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                CoroutineScope(Dispatchers.Main).launch {
                    progressDialog.dismiss()
                    val isOriginal = (type != TYPE_ICON)
                    save(pattern.group(Regex.twimgUrlFileNameGroup), pattern.group(Regex.twimgUrlDotExtGroup), resource!!, isOriginal)
                }
                return false
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            Glide.with(this@ImageFragmentActivity).`as`(ByteArray::class.java).load(imgUrl).listener(requestListener).submit().get()
        }
    }

    private fun save(fileName: String, type: String, byteImage: ByteArray, isOriginal: Boolean) {
        val saveDir = Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DOWNLOADS
        val imgPath = saveDir + "/" + fileName + type.replace(Regex(":orig$"), "")

        if (File(imgPath).exists()) {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.error_file_is_already_exists)
                setItems(arrayOf(
                        getString(R.string.overwrite),
                        getString(R.string.save_as),
                        getString(R.string.cancel)
                )) { _, which ->
                    when (which) {
                        0 -> output(imgPath, byteImage, isOriginal)
                        1 -> {
                            val edit = EditText(this@ImageFragmentActivity)
                            edit.setText(fileName)
                            AlertDialog.Builder(this@ImageFragmentActivity).also {
                                it.setTitle(R.string.save_as)
                                it.setView(edit)
                                it.setNegativeButton(R.string.cancel, null)
                                it.setPositiveButton(R.string.ok) { _, _ ->
                                    val newPath = "$saveDir/${edit.text}$type"
                                    if (File(newPath).exists()) {
                                        save(fileName, type, byteImage, isOriginal)
                                    } else {
                                        output(newPath, byteImage, isOriginal)
                                    }
                                }
                                it.show()
                            }
                        }
                    }
                }
                show()
            }
        } else {
            output(imgPath, byteImage, isOriginal)
        }
    }

    private fun output(imgPath: String, byteImage: ByteArray, isOriginal: Boolean) {
        try {
            FileOutputStream(imgPath, true).apply {
                write(byteImage)
                close()
            }
        } catch (e: IOException) {
            ShowToast(applicationContext, R.string.error_save)
            return
        }
        val message = if (isOriginal) R.string.param_saved_original else R.string.param_saved
        ShowToast(applicationContext, message, imgPath)
    }

    private fun hasWriteExternalStoragePermission(): Boolean {
        val writeExternalStorage = PermissionChecker.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return writeExternalStorage == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 364)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 364) {
            return
        }
        if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage(urls[showImagePager.currentItem])
        } else {
            ShowToast(applicationContext, R.string.permission_rejected)
        }
    }

}