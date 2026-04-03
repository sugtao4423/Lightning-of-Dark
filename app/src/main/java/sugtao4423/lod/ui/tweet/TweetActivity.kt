package sugtao4423.lod.ui.tweet

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import sugtao4423.lod.R
import sugtao4423.lod.databinding.ActivityTweetBinding
import sugtao4423.lod.playing_music_data.PlayingMusicData
import sugtao4423.lod.ui.LoDBaseActivity
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
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

        val binding = ActivityTweetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            val fontAwesome = viewModel.fontAwesomeTypeface
            micButton.typeface = fontAwesome
            musicButton.typeface = fontAwesome
            textOptionButton.typeface = fontAwesome
            tweetButton.typeface = fontAwesome
            imageSelectButton.typeface = fontAwesome
            closeButton.typeface = fontAwesome

            accountScreenName.text = viewModel.accountScreenName

            micButton.setOnClickListener { requestSpeechInput() }
            musicButton.setOnClickListener { appendPlayingMusicData() }
            textOptionButton.setOnClickListener { showTextOptionDialog() }
            tweetButton.setOnClickListener { viewModel.clickTweet() }
            imageSelectButton.setOnClickListener { viewModel.clickImageSelect() }
            closeButton.setOnClickListener { viewModel.clickClose() }

            tweetEdit.doOnTextChanged { text, _, _, _ ->
                viewModel.onChangeTweetText(text ?: "")
            }
            tweetEdit.doAfterTextChanged {
                viewModel.tweetText.value = it?.toString() ?: ""
                viewModel.afterChangeTweetText(it ?: return@doAfterTextChanged)
            }
        }

        viewModel.actionBarTitle.observe(this) {
            if (it == null) supportActionBar?.hide() else supportActionBar?.title = getString(it)
        }
        viewModel.isShowOriginStatus.observe(this) {
            binding.originStatus.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.tweetText.observe(this) {
            binding.tweetEdit.setText(it)
        }
        viewModel.textSelectionEnd.observe(this) {
            if (it == true) {
                binding.tweetEdit.setSelection(binding.tweetEdit.text.length)
            }
        }
        viewModel.remainingTextCount.observe(this) {
            binding.remainingCount.text = it.toString()
        }
        viewModel.isValidTextCount.observe(this) {
            binding.remainingCount.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (it) R.color.tweetTextRemainCount else R.color.tweetTextRemainCountError
                )
            )
        }
        viewModel.selectedImage.observe(this) {
            binding.selectedImageView.setImageURI(it)
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
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                FILE_PERMISSION_REQUEST_CODE
            )
        }
        viewModel.onPickImage.observe(this) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startForResultImagePick.launch(intent)
        }

        viewModel.externalText = intent.getStringExtra(INTENT_EXTRA_KEY_TEXT)
        viewModel.toStatus = intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS).let {
            if (it == null) null else (it as Status)
        }
        viewModel.tweetType = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, TYPE_NEWTWEET)
    }

    private fun requestSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_input))
        }
        startForResultSpeech.launch(intent)
    }

    private fun appendPlayingMusicData() {
        val playingMusicData = PlayingMusicData(this).getPlayingMusicData()
        viewModel.onGotPlayingMusicData(playingMusicData)
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
