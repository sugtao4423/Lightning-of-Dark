package sugtao4423.lod.ui.tweet

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.R
import sugtao4423.lod.databinding.ActivityTweetBinding
import sugtao4423.lod.playing_music_data.PlayingMusicData
import sugtao4423.lod.ui.LoDBaseActivity
import sugtao4423.lod.ui.adapter.SelectedMediaAdapter
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.loadUri
import sugtao4423.twitter4j.Status

class TweetActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_TYPE = "type"
        const val INTENT_EXTRA_KEY_TEXT = "text"
        const val INTENT_EXTRA_KEY_STATUS = "status"

        const val TYPE_NEWTWEET = 0
        const val TYPE_REPLY = 1
        const val TYPE_QUOTERT = 2
        const val TYPE_UNOFFICIALRT = 3
        const val TYPE_PAKUTSUI = 4
        const val TYPE_EXTERNALTEXT = 5
    }

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(TweetActivityViewModel.MAX_MEDIA_COUNT)
    ) { viewModel.onMediaPicked(it) }

    private val startForResultSpeech =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            viewModel.onSpeeched(result)
        }

    private val viewModel: TweetActivityViewModel by viewModels()
    private val binding by lazy { ActivityTweetBinding.inflate(layoutInflater) }
    private val selectedMediaAdapter by lazy { SelectedMediaAdapter(viewModel::onMediaChanged) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        setContentView(binding.root)

        binding.apply {
            val fontAwesome = viewModel.fontAwesomeTypeface
            micButton.typeface = fontAwesome
            musicButton.typeface = fontAwesome
            textOptionButton.typeface = fontAwesome
            tweetButton.typeface = fontAwesome
            imageSelectButton.typeface = fontAwesome
            closeButton.typeface = fontAwesome

            selectedMedias.adapter = selectedMediaAdapter

            micButton.setOnClickListener { requestSpeechInput() }
            musicButton.setOnClickListener { appendPlayingMusicData() }
            textOptionButton.setOnClickListener { showTextOptionDialog() }
            tweetButton.setOnClickListener { viewModel.clickTweet() }
            imageSelectButton.setOnClickListener { pickMedia() }
            closeButton.setOnClickListener { viewModel.clickClose() }

            tweetEdit.doAfterTextChanged {
                viewModel.onTweetTextChanged(it?.toString() ?: "")
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.selectedMedias.collect(selectedMediaAdapter::submit) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }

        viewModel.externalText = intent.getStringExtra(INTENT_EXTRA_KEY_TEXT)
        viewModel.toStatus = intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS) as? Status
        viewModel.tweetType = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, TYPE_NEWTWEET)
    }

    private fun render(state: TweetUiState) {
        state.actionBarTitle?.let {
            supportActionBar?.title = getString(it)
        } ?: supportActionBar?.hide()
        binding.accountScreenName.text = state.accountScreenName

        if (state.tweetText != binding.tweetEdit.text.toString()) {
            binding.tweetEdit.setText(state.tweetText)
        }
        binding.tweetEdit.prefixLength = state.prefixLength
        binding.remainingCount.text = state.remainingTextCount.toString()
        binding.remainingCount.setTextColor(
            ContextCompat.getColor(
                this,
                if (state.isValidTextCount) R.color.tweetTextRemainCount else R.color.tweetTextRemainCountError
            )
        )

        if (state.selectedMedia == null) {
            binding.selectedMediaImage.setImageDrawable(null)
        } else {
            binding.selectedMediaImage.loadUri(state.selectedMedia)
        }
    }

    private fun handleEvent(event: TweetEvent) = when (event) {
        is TweetEvent.Finish -> finish()
        is TweetEvent.ShowOriginStatus -> {
            TweetListAdapter(this).apply {
                add(event.status)
                binding.originStatus.adapter = this
            }
            binding.originStatus.visibility = View.VISIBLE
        }

        is TweetEvent.SetTextSelectionEnd -> binding.tweetEdit.setSelection(binding.tweetEdit.text!!.length)
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

    private fun pickMedia() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            .setOrderedSelection(true)
            .build()
        pickMedia.launch(request)
    }

}
