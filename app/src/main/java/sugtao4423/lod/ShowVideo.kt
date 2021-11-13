package sugtao4423.lod

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import sugtao4423.lod.databinding.ShowVideoBinding
import sugtao4423.support.progressdialog.ProgressDialog

class ShowVideo : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_URL = "URL"

        const val TYPE_VIDEO = 0
        const val TYPE_GIF = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ShowVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (app.getOptions().isVideoOrientationSensor) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        val url = intent.getStringExtra(INTENT_EXTRA_KEY_URL)
        val type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1)
        if (type == -1) {
            finish()
            return
        }

        binding.twVideo.setMediaController(MediaController(this))

        val progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.loading))
            isIndeterminate = false
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(true)
            show()
        }

        binding.twVideo.setVideoURI(Uri.parse(url))
        binding.twVideo.setOnPreparedListener {
            progressDialog.dismiss()
            binding.twVideo.start()
        }

        binding.twVideo.setOnCompletionListener {
            if (type == TYPE_GIF) {
                binding.twVideo.seekTo(0)
                binding.twVideo.start()
            } else {
                finish()
            }
        }
    }

}