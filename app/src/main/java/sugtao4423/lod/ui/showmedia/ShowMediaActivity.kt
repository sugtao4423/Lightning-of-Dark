package sugtao4423.lod.ui.showmedia

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import sugtao4423.lod.R
import sugtao4423.lod.databinding.ActivityShowMediaBinding
import sugtao4423.lod.ui.LoDBaseActivity
import sugtao4423.lod.ui.copyToClipboard
import sugtao4423.lod.ui.showmedia.fragment.ShowImageFragment
import sugtao4423.lod.ui.showmedia.fragment.ShowVideoFragment
import sugtao4423.lod.utils.ChromeIntent
import sugtao4423.twitter4j.MediaEntity

class ShowMediaActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_MEDIA_ENTITIES = "media_entities"
        const val INTENT_EXTRA_KEY_URLS = "urls"
        const val INTENT_EXTRA_KEY_POSITION = "position"

        private const val INFO_HIDE_DURATION = 300L
        private const val INFO_SHOW_DURATION = INFO_HIDE_DURATION / 2
        private const val INFO_AUTO_HIDE_DELAY = 800L
    }

    private val viewModel: ShowMediaActivityViewModel by viewModels()
    private val binding by lazy { ActivityShowMediaBinding.inflate(layoutInflater) }

    private val viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            viewModel.currentPageIndex = position
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        intent.also {
            @Suppress("UNCHECKED_CAST") val mediaEntities =
                it.getSerializableExtra(INTENT_EXTRA_KEY_MEDIA_ENTITIES) as? ArrayList<MediaEntity>
            val urls = it.getStringArrayExtra(INTENT_EXTRA_KEY_URLS)
            val position = it.getIntExtra(INTENT_EXTRA_KEY_POSITION, 0)
            viewModel.setIntentData(mediaEntities, urls, position)
        }

        binding.viewPager.also {
            val adapter = ShowMediaFragmentStateAdapter()
            it.adapter = adapter
            it.offscreenPageLimit = adapter.itemCount
            it.setCurrentItem(viewModel.currentPageIndex, false)
            it.registerOnPageChangeCallback(viewPagerCallback)
        }
        binding.optionButton.setOnClickListener { showOptions() }
        showInfo(true)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: ShowMediaUiState) {
        binding.pageText.text = state.pageText
        requestedOrientation = if (state.enableSensorOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun showOptions() = AlertDialog.Builder(this).also {
        it.setItems(R.array.image_option) { _, which ->
            val url = viewModel.showMedia[viewModel.currentPageIndex].url
            when (which) {
                0 -> ChromeIntent(this, url.toUri())
                1 -> copyToClipboard(url)
                2 -> viewModel.saveCurrentMedia()
            }
        }
        it.show()
    }

    private val hideInfoRunnable = Runnable {
        binding.info.animate().alpha(0f).setDuration(INFO_HIDE_DURATION).start()
    }

    private fun showInfo(autoHide: Boolean) = binding.info.also {
        it.removeCallbacks(hideInfoRunnable)
        it.animate().cancel()
        it.animate().alpha(1f).setDuration(INFO_SHOW_DURATION).start()
        if (autoHide) it.postDelayed(hideInfoRunnable, INFO_AUTO_HIDE_DELAY)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> showInfo(false)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> showInfo(true)
        }
        return super.dispatchTouchEvent(ev)
    }

    inner class ShowMediaFragmentStateAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = viewModel.showMedia.size
        override fun createFragment(position: Int): Fragment =
            when (val media = viewModel.showMedia[position]) {
                is ShowMedia.Image -> ShowImageFragment().also {
                    it.arguments = Bundle().apply {
                        putString(ShowImageFragment.BUNDLE_KEY_ORIGINAL_URL, media.url)
                        putString(ShowImageFragment.BUNDLE_KEY_THUMBNAIL_URL, media.thumbnailUrl)
                    }
                }

                is ShowMedia.Video -> ShowVideoFragment().also {
                    it.arguments = Bundle().apply {
                        putString(ShowVideoFragment.BUNDLE_KEY_URL, media.url)
                        putBoolean(ShowVideoFragment.BUNDLE_KEY_REPEAT, media.repeat)
                    }
                }
            }
    }

}
