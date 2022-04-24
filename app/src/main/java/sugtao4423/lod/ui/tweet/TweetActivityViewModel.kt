package sugtao4423.lod.ui.tweet

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.style.ForegroundColorSpan
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent
import com.twitter.twittertext.Extractor
import com.twitter.twittertext.TwitterTextParser
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.playing_music_data.MusicDataKey
import twitter4j.Status
import twitter4j.StatusUpdate
import kotlin.math.round

class TweetActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    val fontAwesomeTypeface = app.fontAwesomeTypeface
    val accountScreenName = "@" + app.account.screenName

    var tweetType: Int = 0
        set(value) {
            field = value
            onSetTweetType()
        }
    var toStatus: Status? = null
        set(value) {
            field = when {
                value == null -> null
                value.isRetweet -> value.retweetedStatus
                else -> value
            }
        }
    var externalText: String? = null

    private val _actionBarTitle = LiveEvent<Int?>()
    val actionBarTitle: LiveData<Int?> = _actionBarTitle

    val isShowOriginStatus = ObservableField<Boolean>()

    val tweetText = ObservableField("")
    val textSelectionEnd = ObservableField(true)
    val remainingTextCount = ObservableField(140)
    val isValidTextCount = ObservableField(true)

    val selectedImage = ObservableField<Uri?>()

    private val _onSetTweetListAdapter = LiveEvent<Unit>()
    val onSetTweetListAdapter: LiveData<Unit> = _onSetTweetListAdapter

    private val _onFinish = LiveEvent<Unit>()
    val onFinish: LiveData<Unit> = _onFinish

    private val _onRequestReadExternalStoragePermission = LiveEvent<Unit>()
    val onRequestReadExternalStoragePermission: LiveData<Unit> =
        _onRequestReadExternalStoragePermission

    private val _onPickImage = LiveEvent<Unit>()
    val onPickImage: LiveData<Unit> = _onPickImage

    private val _onSpeechInput = LiveEvent<Unit>()
    val onSpeechInput: LiveData<Unit> = _onSpeechInput

    private val _onRequestPlayingMusicData = LiveEvent<Unit>()
    val onRequestPlayingMusicData: LiveData<Unit> = _onRequestPlayingMusicData

    private val _showTextOptionDialog = LiveEvent<Unit>()
    val showTextOptionDialog: LiveData<Unit> = _showTextOptionDialog

    private fun onSetTweetType() {
        _actionBarTitle.value = when (tweetType) {
            TweetActivity.TYPE_REPLY, TweetActivity.TYPE_REPLYALL, TweetActivity.TYPE_QUOTERT -> null
            TweetActivity.TYPE_UNOFFICIALRT -> R.string.unofficial_rt
            else -> R.string.new_tweet
        }

        when (tweetType) {
            TweetActivity.TYPE_REPLY, TweetActivity.TYPE_REPLYALL, TweetActivity.TYPE_QUOTERT -> {
                isShowOriginStatus.set(true)
                _onSetTweetListAdapter.value = Unit
            }
            else -> isShowOriginStatus.set(false)
        }

        when (tweetType) {
            TweetActivity.TYPE_REPLY -> {
                tweetText.set("@${toStatus!!.user.screenName} ")
            }
            TweetActivity.TYPE_REPLYALL -> {
                val mentionUsers = arrayListOf(toStatus!!.user.screenName)
                toStatus!!.userMentionEntities.filter {
                    it.screenName != app.account.screenName && !mentionUsers.contains(it.screenName)
                }.map { it.screenName }.let { mentionUsers.addAll(it) }
                val replyUserScreenNames = mentionUsers.joinToString(" @", "@") + " "
                tweetText.set(replyUserScreenNames)
            }
            TweetActivity.TYPE_QUOTERT -> {
                val quote =
                    " https://twitter.com/${toStatus!!.user.screenName}/status/${toStatus!!.id}"
                tweetText.set(quote)
                textSelectionEnd.set(false)
            }
            TweetActivity.TYPE_UNOFFICIALRT -> {
                val unOfficial = " RT @${toStatus!!.user.screenName}: ${toStatus!!.text}"
                tweetText.set(unOfficial)
                textSelectionEnd.set(false)
            }
            TweetActivity.TYPE_PAKUTSUI -> {
                tweetText.set(toStatus!!.text)
            }
            TweetActivity.TYPE_EXTERNALTEXT -> {
                tweetText.set(externalText)
            }
        }
    }

    fun onChangeTweetText(string: CharSequence) {
        val parseResult = TwitterTextParser.parseTweet(string.toString())
        val length140 = parseResult.weightedLength.let {
            if (it % 2 == 0) it / 2 else (it + 1) / 2
        }
        remainingTextCount.set(140 - length140)
        isValidTextCount.set(parseResult.isValid || length140 == 0)
    }

    fun afterChangeTweetText(editable: Editable) {
        val entityColor = ContextCompat.getColor(app, R.color.twitterBrand)
        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java).forEach {
            editable.removeSpan(it)
        }
        Extractor().extractEntitiesWithIndices(editable.toString()).forEach {
            editable.setSpan(ForegroundColorSpan(entityColor), it.start, it.end, 0)
        }
    }

    fun clickClose() {
        _onFinish.value = Unit
    }

    fun clickTweet() {
        val statusUpdate = StatusUpdate(tweetText.get()!!)

        if (selectedImage.get() != null) {
            val cursor = app.contentResolver.query(selectedImage.get()!!, null, null, null, null)
            if (cursor == null || !cursor.moveToFirst()) {
                ShowToast(app, R.string.error_select_picture)
                return
            }
            val fileName = cursor.let {
                it.moveToFirst()
                val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                val fileName = it.getString(idx)
                it.close()
                fileName
            }
            val inputStream = app.contentResolver.openInputStream(selectedImage.get()!!)
            statusUpdate.media(fileName, inputStream!!)
        }
        if (tweetType == TweetActivity.TYPE_REPLY || tweetType == TweetActivity.TYPE_REPLYALL) {
            app.updateStatus(statusUpdate.inReplyToStatusId(toStatus!!.id))
        } else {
            app.updateStatus(statusUpdate)
        }

        _onFinish.value = Unit
    }

    fun clickImageSelect() {
        if (!hasReadExternalStoragePermission()) {
            _onRequestReadExternalStoragePermission.value = Unit
            return
        }
        _onPickImage.value = Unit
    }

    fun onImagePicked(result: ActivityResult?) {
        if (result?.resultCode != Activity.RESULT_OK || result.data == null) return

        val imageUri = result.data!!.data
        if (imageUri != null) {
            imageUri.let { selectedImage.set(it) }
            ShowToast(app, R.string.success_select_picture)
        } else {
            ShowToast(app, R.string.error_select_picture)
        }
    }

    fun clickMic() {
        _onSpeechInput.value = Unit
    }

    fun onSpeeched(result: ActivityResult?) {
        if (result?.resultCode != Activity.RESULT_OK || result.data == null) return

        val results =
            result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: arrayListOf("")
        tweetText.set(tweetText.get() + results[0])
        textSelectionEnd.apply {
            set(true)
            notifyChange()
        }
    }

    fun clickMusic() {
        _onRequestPlayingMusicData.value = Unit
    }

    fun onGotPlayingMusicData(playingMusicData: HashMap<MusicDataKey, String>?) {
        if (playingMusicData == null) return

        val title = playingMusicData[MusicDataKey.TITLE]!!
        val artist = playingMusicData[MusicDataKey.ARTIST]!!
        val album = playingMusicData[MusicDataKey.ALBUM]!!
        val nowPlayingFormat = app.prefRepository.nowPlayingFormat.ifEmpty {
            "%artist% - %track% #nowplaying"
        }
        val str = nowPlayingFormat
            .replace("%track%", title)
            .replace("%artist%", artist)
            .replace("%album%", album)

        tweetText.set(tweetText.get() + str)
        textSelectionEnd.apply {
            set(true)
            notifyChange()
        }
    }

    fun clickTextOption() {
        _showTextOptionDialog.value = Unit
    }

    fun textOptionOmatase() {
        val chars = tweetText.get()!!.toCharArray()
        val joined = chars.joinToString("　")
        tweetText.set(joined)
        textSelectionEnd.apply {
            set(true)
            notifyChange()
        }
    }

    fun textOptionTotsuzenNoShi() {
        fun stringSize(text: String): Double = text.toCharArray().fold(0.0) { buf, value ->
            buf + if (value.toString().toByteArray().size <= 1) .5 else 1.0
        }

        val lines = tweetText.get()!!.split("\n")
        val maxWidthLength = lines.maxOf { stringSize(it) }
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

        tweetText.set(dead)
        textSelectionEnd.apply {
            set(true)
            notifyChange()
        }
    }

    private fun hasReadExternalStoragePermission(): Boolean {
        val writeExternalStorage = PermissionChecker.checkSelfPermission(
            app, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return writeExternalStorage == PackageManager.PERMISSION_GRANTED
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == TweetActivity.FILE_PERMISSION_REQUEST_CODE &&
            permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            clickImageSelect()
        } else {
            ShowToast(app, R.string.permission_rejected)
        }
    }

}
