package sugtao4423.lod.ui.showvideo

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.MediaController
import androidx.activity.viewModels
import sugtao4423.lod.ui.LoDBaseActivity
import sugtao4423.lod.R
import sugtao4423.lod.databinding.ShowVideoBinding
import sugtao4423.support.progressdialog.ProgressDialog

class ShowVideoActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_URL = "url"

        const val TYPE_VIDEO = 0
        const val TYPE_GIF = 1
    }

    private var progressDialog: ProgressDialog? = null

    private val viewModel: ShowVideoActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ShowVideoBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
            it.mediaController = MediaController(this)
        }
        setContentView(binding.root)

        if (viewModel.isVideoOrientationSensor) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        viewModel.videoUrl = intent.getStringExtra(INTENT_EXTRA_KEY_URL)
        viewModel.videoType = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1)
        if (viewModel.videoType == -1) {
            finish()
            return
        }

        viewModel.onStopProgressDialog.observe(this) {
            if (progressDialog != null) progressDialog!!.dismiss()
        }
        viewModel.onFinish.observe(this) {
            finish()
        }

        showProgressDialog()
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.loading))
            isIndeterminate = false
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(true)
            show()
        }
    }

}
