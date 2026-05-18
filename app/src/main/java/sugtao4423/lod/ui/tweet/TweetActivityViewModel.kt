package sugtao4423.lod.ui.tweet

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResult
import androidx.core.content.PermissionChecker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hadilq.liveevent.LiveEvent
import com.twitter.twittertext.TwitterTextParser
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.playing_music_data.MusicDataKey
import sugtao4423.lod.utils.showToast
import sugtao4423.lod.utils.toStatusUrl
import sugtao4423.twitter4j.Status
import sugtao4423.twitterweb4j.model.CreateTweet
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

    val isShowOriginStatus = MutableLiveData<Boolean>()

    val tweetText = MutableLiveData("")
    val prefixLength = MutableLiveData(0)
    val textSelectionEnd = MutableLiveData(true)
    val remainingTextCount = MutableLiveData(140)
    val isValidTextCount = MutableLiveData(true)

    val selectedMedia = MutableLiveData<Uri?>()

    private val _onSetTweetListAdapter = LiveEvent<Unit>()
    val onSetTweetListAdapter: LiveData<Unit> = _onSetTweetListAdapter

    private val _onFinish = LiveEvent<Unit>()
    val onFinish: LiveData<Unit> = _onFinish

    private val _onRequestReadExternalStoragePermission = LiveEvent<Unit>()
    val onRequestReadExternalStoragePermission: LiveData<Unit> =
        _onRequestReadExternalStoragePermission

    private val _onPickMedia = LiveEvent<Unit>()
    val onPickMedia: LiveData<Unit> = _onPickMedia

    private fun onSetTweetType() {
        _actionBarTitle.value = when (tweetType) {
            TweetActivity.TYPE_REPLY, TweetActivity.TYPE_QUOTERT -> null
            TweetActivity.TYPE_UNOFFICIALRT -> R.string.unofficial_rt
            else -> R.string.new_tweet
        }

        when (tweetType) {
            TweetActivity.TYPE_REPLY, TweetActivity.TYPE_QUOTERT -> {
                isShowOriginStatus.value = true
                _onSetTweetListAdapter.value = Unit
            }

            else -> isShowOriginStatus.value = false
        }

        when (tweetType) {
            TweetActivity.TYPE_REPLY -> {
                val mentionUsers =
                    setOf(toStatus!!.user.screenName) + toStatus!!.userMentionEntities.filter {
                        it.id != app.account.id
                    }.map { it.screenName }.toSet()
                val replyUserScreenNames = mentionUsers.joinToString(" @", "@") + " "
                replyUserScreenNames.also {
                    tweetText.value = it
                    prefixLength.value = it.length
                }
            }

            TweetActivity.TYPE_UNOFFICIALRT -> {
                val unOfficial = " RT @${toStatus!!.user.screenName}: ${toStatus!!.text}"
                tweetText.value = unOfficial
                textSelectionEnd.value = false
            }

            TweetActivity.TYPE_PAKUTSUI -> {
                tweetText.value = toStatus!!.text
            }

            TweetActivity.TYPE_EXTERNALTEXT -> {
                tweetText.value = externalText
            }
        }
    }

    fun onChangeTweetText(string: CharSequence) {
        val parseResult = TwitterTextParser.parseTweet(string.toString())
        val length140 = parseResult.weightedLength.let {
            if (it % 2 == 0) it / 2 else (it + 1) / 2
        }
        remainingTextCount.value = 140 - length140
        isValidTextCount.value = parseResult.isValid || length140 == 0
    }

    fun clickClose() {
        _onFinish.value = Unit
    }

    fun clickTweet() {
        val text = tweetText.value!!.substring(prefixLength.value!!)
        val createTweet = CreateTweet(text)

        when (tweetType) {
            TweetActivity.TYPE_REPLY -> createTweet.inReplyToStatusId = toStatus!!.id
            TweetActivity.TYPE_QUOTERT -> createTweet.attachmentUrl = toStatus!!.toStatusUrl()
        }
        app.updateStatus(createTweet, selectedMedia.value)

        _onFinish.value = Unit
    }

    fun clickMediaSelect() {
        if (!hasReadExternalStoragePermission()) {
            _onRequestReadExternalStoragePermission.value = Unit
            return
        }
        _onPickMedia.value = Unit
    }

    fun onMediaPicked(result: ActivityResult?) {
        if (result?.resultCode != Activity.RESULT_OK || result.data == null) return

        val mediaUri = result.data!!.data
        if (mediaUri != null) {
            canUploadMedia(mediaUri)?.let {
                app.showToast(it)
                return
            }
            selectedMedia.value = mediaUri
            app.showToast(R.string.success_select_media)
        } else {
            app.showToast(R.string.error_select_media)
        }
    }

    fun onSpeeched(result: ActivityResult?) {
        if (result?.resultCode != Activity.RESULT_OK || result.data == null) return

        val results =
            result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: arrayListOf("")
        tweetText.value += results[0]
        textSelectionEnd.value = true
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

        tweetText.value += str
        textSelectionEnd.value = true
    }

    fun textOptionOmatase() {
        val chars = tweetText.value!!.toCharArray()
        val joined = chars.joinToString("　")
        tweetText.value = joined
        textSelectionEnd.value = true
    }

    fun textOptionTotsuzenNoShi() {
        fun stringSize(text: String): Double = text.toCharArray().fold(0.0) { buf, value ->
            buf + if (value.toString().toByteArray().size <= 1) .5 else 1.0
        }

        val lines = tweetText.value!!.split("\n")
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

        tweetText.value = dead
        textSelectionEnd.value = true
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
            clickMediaSelect()
        } else {
            app.showToast(R.string.permission_rejected)
        }
    }

    private fun canUploadMedia(uri: Uri): Int? {
        val mimeType = app.contentResolver.getType(uri)
            ?: return R.string.error_select_media
        if (mimeType == "image/gif" || mimeType.startsWith("video/")) return null

        app.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            val size = cursor.getColumnIndex(OpenableColumns.SIZE).let {
                cursor.getLong(it)
            }
            if (size > 5 * 1024 * 1024) {
                return R.string.error_select_image_large
            }
        }
        return null
    }

}
