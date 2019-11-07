package sugtao4423.lod.swipe_image_viewer

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.view.PagerTabStrip
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.Window
import android.widget.EditText
import com.tenthbit.view.ZoomViewPager
import sugtao4423.lod.ChromeIntent
import sugtao4423.lod.LoDBaseActivity
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.utils.Regex
import sugtao4423.support.progressdialog.ProgressDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ImageFragmentActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_URLS = "urls"
        const val INTENT_EXTRA_KEY_POSITION = "position"

        const val TYPE_ICON = 0
        const val TYPE_BANNER = 1
    }

    private lateinit var adapter: ImagePagerAdapter
    private lateinit var pager: ZoomViewPager
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

        pager = findViewById<ZoomViewPager>(R.id.showImagePager).also {
            it.adapter = adapter
            it.offscreenPageLimit = urls.size - 1
            it.currentItem = pos
        }

        findViewById<PagerTabStrip>(R.id.showImagePagerTabStrip).apply {
            tabIndicatorColor = ContextCompat.getColor(applicationContext, R.color.pagerTabText)
            drawFullUnderline = true
        }
    }

    fun clickImageOption(@Suppress("UNUSED_PARAMETER") v: View) {
        val imageUrl = urls[pager.currentItem]
        AlertDialog.Builder(this).apply {
            setItems(arrayOf(getString(R.string.open_in_browser), getString(R.string.save))) { _, which ->
                when (which) {
                    0 -> ChromeIntent(this@ImageFragmentActivity, Uri.parse(imageUrl))
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
            val bannerImg = (adapter.getItem(pager.currentItem) as ImageFragment).nonOrigImage
            save(banner.group(Regex.userBannerUrlFileNameGroup), ".jpg", bannerImg, false)
            return
        }

        val imgUrl = imageUrl + (if (type == TYPE_ICON) "" else ":orig")

        val pattern = Regex.twimgUrl.matcher(imgUrl)
        if (!pattern.find()) {
            ShowToast(applicationContext, R.string.url_not_match_pattern_and_dont_save)
            return
        }

        object : AsyncTask<Unit, Unit, ByteArray?>() {
            private lateinit var progressDialog: ProgressDialog

            override fun onPreExecute() {
                progressDialog = ProgressDialog(this@ImageFragmentActivity).apply {
                    setMessage(getString(R.string.loading))
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(true)
                    show()
                }
            }

            override fun doInBackground(vararg params: Unit?): ByteArray? {
                return try {
                    val connection = (URL(imgUrl).openConnection() as HttpsURLConnection).apply {
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
                    val result = bout.toByteArray()
                    inputStream.close()
                    bout.close()
                    connection.disconnect()
                    result
                } catch (e: IOException) {
                    null
                }
            }

            override fun onPostExecute(result: ByteArray?) {
                if (result != null) {
                    progressDialog.dismiss()
                    val isOriginal = (type != TYPE_ICON)
                    save(pattern.group(Regex.twimgUrlFileNameGroup), pattern.group(Regex.twimgUrlDotExtGroup), result, isOriginal)
                } else {
                    ShowToast(applicationContext, R.string.error_get_original_image)
                }
            }
        }.execute()
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
        val message = if (isOriginal) R.string.saved_original else R.string.saved
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
            saveImage(urls[pager.currentItem])
        } else {
            ShowToast(applicationContext, R.string.permission_rejected)
        }
    }

}