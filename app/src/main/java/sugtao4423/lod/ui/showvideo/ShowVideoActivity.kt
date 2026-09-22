package sugtao4423.lod.ui.showvideo

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.databinding.ActivityShowVideoBinding
import sugtao4423.lod.ui.LoDBaseActivity

class ShowVideoActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_URL = "url"

        const val TYPE_VIDEO = 0
        const val TYPE_GIF = 1
    }

    private val viewModel: ShowVideoActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShowVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerView.player = viewModel.exoPlayer

        if (viewModel.isVideoOrientationSensor) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        viewModel.videoUrl = intent.getStringExtra(INTENT_EXTRA_KEY_URL)
        viewModel.videoType = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1)
        if (viewModel.videoType == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect(::handleEvent)
            }
        }
    }

    private fun handleEvent(event: ShowVideoEvent) = when (event) {
        ShowVideoEvent.Finish -> finish()
    }

    override fun onStop() {
        super.onStop()
        viewModel.pauseVideo()
    }

}
