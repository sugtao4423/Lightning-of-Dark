package sugtao4423.lod.swipe_image_viewer

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import sugtao4423.lod.ChromeIntent
import sugtao4423.lod.LoDBaseActivity
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.databinding.ShowImagePagerBinding
import sugtao4423.lod.utils.Regex

class ImageFragmentActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_URLS = "urls"
        const val INTENT_EXTRA_KEY_POSITION = "position"

        const val TYPE_ICON = 0
        const val TYPE_BANNER = 1
    }

    private lateinit var binding: ShowImagePagerBinding
    private lateinit var adapter: ImagePagerAdapter
    private lateinit var urls: Array<String>
    private var type = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ShowImagePagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (app.prefRepository.isImageOrientationSensor) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        binding.optionBtn.setOnClickListener { clickImageOption() }

        urls = intent.getStringArrayExtra(INTENT_EXTRA_KEY_URLS)!!
        type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1)
        val pos = intent.getIntExtra(INTENT_EXTRA_KEY_POSITION, 0)
        adapter = ImagePagerAdapter(supportFragmentManager, urls)

        binding.showImagePager.also {
            it.adapter = adapter
            it.offscreenPageLimit = urls.size - 1
            it.currentItem = pos
        }

        binding.showImagePagerTabStrip.apply {
            tabIndicatorColor = ContextCompat.getColor(applicationContext, R.color.pagerTabText)
            drawFullUnderline = true
        }
    }

    private fun clickImageOption() {
        val imageUrl = urls[binding.showImagePager.currentItem]
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
        if (type == TYPE_BANNER) {
            val banner = Regex.userBannerUrl.matcher(imageUrl)
            if (!banner.find()) {
                ShowToast(applicationContext, R.string.url_not_match_pattern_and_dont_save)
                return
            }
            val fileName = banner.group(Regex.userBannerUrlFileNameGroup)!! + ".jpg"
            download(imageUrl, fileName, false)
            return
        }

        val imgUrl = imageUrl + (if (type == TYPE_ICON) "" else ":orig")

        val pattern = Regex.twimgUrl.matcher(imgUrl)
        if (!pattern.find()) {
            ShowToast(applicationContext, R.string.url_not_match_pattern_and_dont_save)
            return
        }

        val fileName = pattern.group(Regex.twimgUrlFileNameGroup)!!
        val fileExt = pattern.group(Regex.twimgUrlDotExtGroup)!!.replace(Regex(":orig$"), "")
        download(imgUrl, "$fileName$fileExt", (type != TYPE_ICON))
    }

    private fun download(fileUrl: String, fileName: String, isOriginal: Boolean) {
        val dlManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = if (isOriginal) R.string.param_saved_original else R.string.param_saved
                ShowToast(applicationContext, message, fileName)
                unregisterReceiver(this)
            }
        }
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        val dlRequest = DownloadManager.Request(fileUrl.toUri()).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        dlManager.enqueue(dlRequest)
    }

}
