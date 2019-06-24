package sugtao4423.lod

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import sugtao4423.lod.playing_music_data.MusicDataKey
import sugtao4423.lod.playing_music_data.PlayingMusicData
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import twitter4j.Status
import twitter4j.StatusUpdate
import java.io.File

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

    private lateinit var tweetText: EditText
    private lateinit var moji140: TextView
    private lateinit var status: Status
    private var type = 0
    private var image: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tweet_activity)

        supportActionBar?.setDisplayShowHomeEnabled(false)

        setTypeface()

        val tweetAccount = findViewById<TextView>(R.id.tweetAccount)
        tweetAccount.text = "@" + app.getCurrentAccount().screenName

        tweetText = findViewById(R.id.tweetText)
        moji140 = findViewById(R.id.moji140)

        if (intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS) != null) {
            status = intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS) as Status
            status = if (status.isRetweet) status.retweetedStatus else status
        }

        type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, 0)

        var setSelectionEnd = false

        val originStatus = findViewById<TweetListView>(R.id.originStatus)
        when (type) {
            TYPE_REPLY, TYPE_REPLYALL, TYPE_QUOTERT -> {
                supportActionBar?.hide()
                originStatus.visibility = View.VISIBLE
                originStatus.isFocusable = false
                TweetListAdapter(this).apply {
                    onItemClickListener = ListViewListener()
                    onItemLongClickListener = ListViewListener()
                    add(status)
                    originStatus.adapter = this
                }
            }
            else -> originStatus.visibility = View.GONE
        }

        when (type) {
            TYPE_NEWTWEET -> supportActionBar?.setTitle(R.string.new_tweet)
            TYPE_REPLY -> {
                tweetText.setText("@${status.user.screenName} ")
                setSelectionEnd = true
            }
            TYPE_REPLYALL -> {
                val mentionUsers = arrayListOf<String>()
                mentionUsers.add(status.user.screenName)
                status.userMentionEntities.map {
                    if (it.screenName != app.getCurrentAccount().screenName && !mentionUsers.contains(it.screenName)) {
                        mentionUsers.add(it.screenName)
                    }
                }
                val replyUserScreenNames = mentionUsers.joinToString(" @", "@") + " "
                tweetText.setText(replyUserScreenNames)
                setSelectionEnd = true
            }
            TYPE_QUOTERT -> {
                val quote = " https://twitter.com/${status.user.screenName}/status/${status.id}"
                tweetText.setText(quote)
            }
            TYPE_UNOFFICIALRT -> {
                supportActionBar?.setTitle(R.string.unofficial_rt)
                val unOfficial = " RT @${status.user.screenName}: ${status.text}"
                tweetText.setText(unOfficial)
            }
            TYPE_PAKUTSUI -> {
                supportActionBar?.setTitle(R.string.new_tweet)
                tweetText.setText(status.text)
                setSelectionEnd = true
            }
            TYPE_EXTERNALTEXT -> {
                supportActionBar?.setTitle(R.string.new_tweet)
                tweetText.setText(intent.getStringExtra(INTENT_EXTRA_KEY_TEXT))
                setSelectionEnd = true
            }
        }

        if (setSelectionEnd) {
            tweetText.setSelection(tweetText.text.count())
        }

        moji140.text = (140 - tweetText.text.count()).toString()
        moji140count()
    }

    private fun setTypeface() {
        val buttons = arrayOf(
                findViewById<Button>(R.id.tweetButton),
                findViewById<Button>(R.id.imageSelect),
                findViewById<Button>(R.id.tweetClose),
                findViewById<Button>(R.id.cursor_start),
                findViewById<Button>(R.id.cursor_end),
                findViewById<Button>(R.id.tweetMic),
                findViewById<Button>(R.id.tweetMusic)
        )
        val tf = app.getFontAwesomeTypeface()
        buttons.map {
            it.typeface = tf
        }
    }

    private fun moji140count() {
        tweetText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                moji140.text = (140 - s!!.count()).toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun tweet(@Suppress("UNUSED_PARAMETER") v: View) {
        findViewById<Button>(R.id.tweetButton).isEnabled = false
        findViewById<Button>(R.id.tweetClose).isEnabled = false
        findViewById<Button>(R.id.imageSelect).isEnabled = false

        val text = tweetText.text.toString()

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

    fun image(@Suppress("UNUSED_PARAMETER") v: View?) {
        if (!hasWriteExternalStoragePermission()) {
            requestWriteExternalStoragePermission()
            return
        }
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_PICK
        }
        startActivityForResult(intent, 810)
    }

    fun mic(@Suppress("UNUSED_PARAMETER") v: View) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_input))
        }
        startActivityForResult(intent, 1919)
    }

    fun music(@Suppress("UNUSED_PARAMETER") v: View) {
        val playingMusicData = PlayingMusicData(this).getPlayingMusicData() ?: return

        val title = playingMusicData[MusicDataKey.TITLE] ?: ""
        val artist = playingMusicData[MusicDataKey.ARTIST] ?: ""
        val album = playingMusicData[MusicDataKey.ALBUM] ?: ""
        var nowplayingFormat = app.getOptions().nowplayingFormat
        if (nowplayingFormat == "") {
            nowplayingFormat = "%artist% - %track% #nowplaying"
        }
        val str = nowplayingFormat
                .replace("%track%", title)
                .replace("%artist%", artist)
                .replace("%album%", album)
        tweetText.setText(tweetText.text.toString() + str)
        tweetText.setSelection(tweetText.text.count())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }

        if (requestCode == 1919 && resultCode == RESULT_OK) { // 音声入力
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            tweetText.setText(tweetText.text.toString() + results[0])
            cursor_end(null)
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
                findViewById<ImageView>(R.id.selectedImage).setImageURI(data.data)
                ShowToast(applicationContext, R.string.success_select_picture)
            } catch (e: Exception) {
                ShowToast(applicationContext, R.string.error_select_picture)
            }
        }
    }

    fun cursor_start(@Suppress("UNUSED_PARAMETER") v: View) {
        tweetText.setSelection(0)
    }

    fun cursor_end(@Suppress("UNUSED_PARAMETER") v: View?) {
        tweetText.setSelection(tweetText.text.count())
    }

    fun back(@Suppress("UNUSED_PARAMETER") v: View) {
        finish()
    }

    private fun hasWriteExternalStoragePermission(): Boolean {
        val writeExternalStorage = PermissionChecker.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return writeExternalStorage == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 364)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 364) {
            return
        }
        if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            image(null)
        } else {
            ShowToast(applicationContext, R.string.permission_rejected)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        image = null
    }

}