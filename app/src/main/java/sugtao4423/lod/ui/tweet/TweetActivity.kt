package sugtao4423.lod.ui.tweet

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import sugtao4423.lod.ui.LoDBaseActivity
import sugtao4423.lod.R
import sugtao4423.lod.databinding.TweetActivityBinding
import sugtao4423.lod.playing_music_data.PlayingMusicData
import sugtao4423.lod.ui.adapter.TweetListAdapter
import twitter4j.Status

class TweetActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_TEXT = "text"
        const val INTENT_EXTRA_KEY_STATUS = "status"

        const val TYPE_NEWTWEET = 0
        const val TYPE_REPLY = 1
        const val TYPE_REPLYALL = 2
        const val TYPE_QUOTERT = 3
        const val TYPE_UNOFFICIALRT = 4
        const val TYPE_PAKUTSUI = 5
        const val TYPE_EXTERNALTEXT = 6

        const val FILE_PERMISSION_REQUEST_CODE = 364364
    }

    private val startForResultImagePick =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            viewModel.onImagePicked(result)
        }

    private val startForResultSpeech =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            viewModel.onSpeeched(result)
        }

    private val viewModel: TweetActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        val binding = TweetActivityBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)

        viewModel.actionBarTitle.observe(this) {
            if (it == null) supportActionBar?.hide() else supportActionBar?.title = getString(it)
        }
        viewModel.onSetTweetListAdapter.observe(this) {
            TweetListAdapter(this).apply {
                add(viewModel.toStatus!!)
                binding.originStatus.adapter = this
            }
        }
        viewModel.onFinish.observe(this) { finish() }
        viewModel.onRequestReadExternalStoragePermission.observe(this) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                FILE_PERMISSION_REQUEST_CODE
            )
        }
        viewModel.onPickImage.observe(this) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startForResultImagePick.launch(intent)
        }
        viewModel.onSpeechInput.observe(this) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_input))
            }
            startForResultSpeech.launch(intent)
        }
        viewModel.onRequestPlayingMusicData.observe(this) {
            val playingMusicData = PlayingMusicData(this).getPlayingMusicData()
            viewModel.onGotPlayingMusicData(playingMusicData)
        }
        viewModel.showTextOptionDialog.observe(this) {
            showTextOptionDialog()
        }

        viewModel.externalText = intent.getStringExtra(INTENT_EXTRA_KEY_TEXT)
        viewModel.toStatus = intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS).let {
            if (it == null) null else (it as Status)
        }
        viewModel.tweetType = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, TYPE_NEWTWEET)
    }

    private fun showTextOptionDialog() {
        AlertDialog.Builder(this).apply {
            setItems(R.array.text_options) { _, which ->
                when (which) {
                    0 -> viewModel.textOptionOmatase()
                    1 -> viewModel.textOptionTotsuzenNoShi()
                }
            }
            show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
