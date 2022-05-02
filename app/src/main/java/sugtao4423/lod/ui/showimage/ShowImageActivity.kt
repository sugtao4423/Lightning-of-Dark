package sugtao4423.lod.ui.showimage

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import sugtao4423.lod.utils.ChromeIntent
import sugtao4423.lod.ui.LoDBaseActivity
import sugtao4423.lod.databinding.ActivityShowImageBinding

class ShowImageActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_URLS = "urls"
        const val INTENT_EXTRA_KEY_POSITION = "position"

        const val TYPE_ICON = 0
        const val TYPE_BANNER = 1
    }

    private val viewModel: ShowImageActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = ActivityShowImageBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)

        if (viewModel.isImageOrientationSensor) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        viewModel.imageUrls = intent.getStringArrayExtra(INTENT_EXTRA_KEY_URLS)!!
        viewModel.imageType = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1)
        viewModel.currentPageIndex = intent.getIntExtra(INTENT_EXTRA_KEY_POSITION, 0)
        val adapter = ShowImageFragmentPagerAdapter(supportFragmentManager, viewModel.imageUrls)

        binding.viewPager.also {
            it.adapter = adapter
            it.offscreenPageLimit = adapter.count - 1
            it.currentItem = viewModel.currentPageIndex
            it.addOnPageChangeListener(viewPagerOnPageChangeListener)
        }

        viewModel.showImageOptionDialog.observe(this) {
            AlertDialog.Builder(this).apply {
                setItems(it.dialogItemRes) { _, which ->
                    when (which) {
                        0 -> ChromeIntent(this@ShowImageActivity, it.openImageUri)
                        1 -> viewModel.saveCurrentImage()
                    }
                }
                show()
            }
        }
    }

    private val viewPagerOnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            viewModel.currentPageIndex = position
        }

        override fun onPageScrollStateChanged(state: Int) {
        }
    }

}
