package sugtao4423.lod

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import kotlinx.android.synthetic.main.show_video.*
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
        setContentView(R.layout.show_video)
        if (app.getOptions().isVideoOrientationSensor) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        val url = intent.getStringExtra(INTENT_EXTRA_KEY_URL)
        val type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1)
        if (type == -1) {
            finish()
            return
        }

        twVideo.setMediaController(MediaController(this))

        val progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.loading))
            isIndeterminate = false
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(true)
            show()
        }

        twVideo.setVideoURI(Uri.parse(url))
        twVideo.setOnPreparedListener {
            progressDialog.dismiss()
            twVideo.start()
        }

        twVideo.setOnCompletionListener {
            if (type == TYPE_GIF) {
                twVideo.seekTo(0)
                twVideo.start()
            } else {
                finish()
            }
        }
    }

}