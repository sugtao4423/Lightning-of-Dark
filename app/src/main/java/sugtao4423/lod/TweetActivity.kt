package sugtao4423.lod

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.twitter.twittertext.Extractor
import com.twitter.twittertext.TwitterTextParser
import sugtao4423.lod.databinding.TweetActivityBinding
import sugtao4423.lod.playing_music_data.MusicDataKey
import sugtao4423.lod.playing_music_data.PlayingMusicData
import sugtao4423.lod.tweetlistview.TweetListAdapter
import twitter4j.Status
import twitter4j.StatusUpdate
import java.io.File
import kotlin.math.max
import kotlin.math.round

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
    }

    private lateinit var binding: TweetActivityBinding
    private lateinit var status: Status
    private var type = 0
    private var image: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TweetActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowHomeEnabled(false)

        setTypeface()

        binding.apply {
            tweetAccount.text = "@" + app.account.screenName
            closeBtn.setOnClickListener { finish() }
            tweetBtn.setOnClickListener { clickTweet() }
            imageSelectBtn.setOnClickListener { clickImageSelect() }
            micBtn.setOnClickListener { clickMic() }
            musicBtn.setOnClickListener { clickMusic() }
            textOptionBtn.setOnClickListener { clickTextOption() }
        }

        text140count()

        if (intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS) != null) {
            status = intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS) as Status
            status = if (status.isRetweet) status.retweetedStatus else status
        }

        type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, 0)

        var setSelectionEnd = false

        when (type) {
            TYPE_REPLY, TYPE_REPLYALL, TYPE_QUOTERT -> {
                supportActionBar?.hide()
                binding.originStatus.visibility = View.VISIBLE
                binding.originStatus.isFocusable = false
                TweetListAdapter(this).apply {
                    add(status)
                    binding.originStatus.adapter = this
                }
            }
            else -> binding.originStatus.visibility = View.GONE
        }

        when (type) {
            TYPE_NEWTWEET -> supportActionBar?.setTitle(R.string.new_tweet)
            TYPE_REPLY -> {
                binding.tweetText.setText("@${status.user.screenName} ")
                setSelectionEnd = true
            }
            TYPE_REPLYALL -> {
                val mentionUsers = arrayListOf<String>()
                mentionUsers.add(status.user.screenName)
                status.userMentionEntities.map {
                    if (it.screenName != app.account.screenName && !mentionUsers.contains(it.screenName)) {
                        mentionUsers.add(it.screenName)
                    }
                }
                val replyUserScreenNames = mentionUsers.joinToString(" @", "@") + " "
                binding.tweetText.setText(replyUserScreenNames)
                setSelectionEnd = true
            }
            TYPE_QUOTERT -> {
                val quote = " https://twitter.com/${status.user.screenName}/status/${status.id}"
                binding.tweetText.setText(quote)
            }
            TYPE_UNOFFICIALRT -> {
                supportActionBar?.setTitle(R.string.unofficial_rt)
                val unOfficial = " RT @${status.user.screenName}: ${status.text}"
                binding.tweetText.setText(unOfficial)
            }
            TYPE_PAKUTSUI -> {
                supportActionBar?.setTitle(R.string.new_tweet)
                binding.tweetText.setText(status.text)
                setSelectionEnd = true
            }
            TYPE_EXTERNALTEXT -> {
                supportActionBar?.setTitle(R.string.new_tweet)
                binding.tweetText.setText(intent.getStringExtra(INTENT_EXTRA_KEY_TEXT))
                setSelectionEnd = true
            }
        }

        if (setSelectionEnd) {
            binding.tweetText.setSelection(binding.tweetText.text.count())
        }
    }

    private fun setTypeface() {
        val buttons: Array<Button> = arrayOf(
                binding.tweetBtn,
                binding.imageSelectBtn,
                binding.closeBtn,
                binding.micBtn,
                binding.musicBtn,
                binding.textOptionBtn
        )
        val tf = app.getFontAwesomeTypeface()
        buttons.map {
            it.typeface = tf
        }
    }

    private fun text140count() {
        val defaultTextViewColors = binding.text140.textColors
        val entityColor = ContextCompat.getColor(applicationContext, R.color.twitterBrand)
        val extractor = Extractor()

        binding.tweetText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val parseResult = TwitterTextParser.parseTweet(s.toString())
                val length140 = parseResult.weightedLength.let {
                    if (it % 2 == 0) {
                        it / 2
                    } else {
                        (it + 1) / 2
                    }
                }
                binding.text140.text = (140 - length140).toString()

                if (parseResult.isValid || length140 == 0) {
                    binding.text140.setTextColor(defaultTextViewColors)
                } else {
                    binding.text140.setTextColor(Color.RED)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                s?.getSpans(0, s.length, ForegroundColorSpan::class.java)?.map {
                    s.removeSpan(it)
                }
                extractor.extractEntitiesWithIndices(s.toString()).map {
                    s?.setSpan(ForegroundColorSpan(entityColor), it.start, it.end, 0)
                }
            }
        })
    }

    private fun clickTweet() {
        binding.tweetBtn.isEnabled = false
        binding.closeBtn.isEnabled = false
        binding.imageSelectBtn.isEnabled = false

        val text = binding.tweetText.text.toString()

        val statusUpdate = StatusUpdate(text)
        if (image != null) {
            statusUpdate.media(image)
        }
        if (type == TYPE_REPLY || type == TYPE_REPLYALL) {
            app.updateStatus(statusUpdate.inReplyToStatusId(status.id))
        } else {
            app.updateStatus(statusUpdate)
        }

        finish()
    }

    private fun clickImageSelect() {
        if (!hasReadExternalStoragePermission()) {
            requestReadExternalStoragePermission()
            return
        }
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_PICK
        }
        startActivityForResult(intent, 810)
    }

    private fun clickMic() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_input))
        }
        startActivityForResult(intent, 1919)
    }

    private fun clickMusic() {
        val playingMusicData = PlayingMusicData(this).getPlayingMusicData() ?: return

        val title = playingMusicData[MusicDataKey.TITLE]!!
        val artist = playingMusicData[MusicDataKey.ARTIST]!!
        val album = playingMusicData[MusicDataKey.ALBUM]!!
        var nowplayingFormat = app.getOptions().nowplayingFormat
        if (nowplayingFormat == "") {
            nowplayingFormat = "%artist% - %track% #nowplaying"
        }
        val str = nowplayingFormat
                .replace("%track%", title)
                .replace("%artist%", artist)
                .replace("%album%", album)
        binding.tweetText.setText(binding.tweetText.text.toString() + str)
        binding.tweetText.setSelection(binding.tweetText.text.count())
    }

    private fun clickTextOption() {
        AlertDialog.Builder(this).apply {
            setItems(R.array.text_options) { _, which ->
                when (which) {
                    0 -> {
                        val chars = binding.tweetText.text.toString().toCharArray()
                        val modified = chars.joinToString("　")
                        binding.tweetText.setText(modified)
                    }
                    1 -> {
                        fun stringSize(text: String): Double {
                            var len = 0.0
                            text.toCharArray().forEach { char ->
                                len += if (char.toString().toByteArray().size <= 1) .5 else 1.0
                            }
                            return len
                        }

                        val lines = binding.tweetText.text.toString().split("\n")
                        var maxWidthLength = 0.0
                        lines.forEach {
                            maxWidthLength = max(maxWidthLength, stringSize((it)))
                        }
                        val repeatCount = round(maxWidthLength).toInt()

                        var dead = "＿" + "人".repeat(repeatCount) + "＿\n"
                        lines.forEach {
                            val spacers = if (stringSize(it) == maxWidthLength) {
                                ""
                            } else {
                                " ".repeat(((repeatCount - stringSize(it)) * 3).toInt())
                            }
                            dead += "＞ ${it}${spacers} ＜\n"
                        }
                        dead += "￣" + "Y^".repeat(repeatCount) + "￣"
                        binding.tweetText.setText(dead)
                    }
                }
                binding.tweetText.setSelection(binding.tweetText.text.count())
            }
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }

        if (requestCode == 1919 && resultCode == RESULT_OK) { // 音声入力
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?: arrayListOf("")
            binding.tweetText.setText(binding.tweetText.text.toString() + results[0])
            binding.tweetText.setSelection(binding.tweetText.text.count())
        }
        if (requestCode == 810 && resultCode == RESULT_OK) { // 画像選択
            try {
                val columns = arrayOf(MediaStore.Images.Media.DATA)
                contentResolver.query(data.data!!, columns, null, null, null).apply {
                    if (this == null) {
                        return
                    }
                    moveToFirst()
                    image = File(getString(0))
                    close()
                }
                binding.selectedImage.setImageURI(data.data)
                ShowToast(applicationContext, R.string.success_select_picture)
            } catch (e: Exception) {
                ShowToast(applicationContext, R.string.error_select_picture)
            }
        }
    }

    private fun hasReadExternalStoragePermission(): Boolean {
        val writeExternalStorage = PermissionChecker.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
        return writeExternalStorage == PackageManager.PERMISSION_GRANTED
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 364)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 364) {
            return
        }
        if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            clickImageSelect()
        } else {
            ShowToast(applicationContext, R.string.permission_rejected)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        image = null
    }

}
