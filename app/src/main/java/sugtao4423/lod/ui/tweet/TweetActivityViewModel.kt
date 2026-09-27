package sugtao4423.lod.ui.tweet

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.AndroidViewModel
import com.twitter.twittertext.TwitterTextParser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.playing_music_data.MusicDataKey
import sugtao4423.lod.utils.showToast
import sugtao4423.lod.utils.toStatusUrl
import sugtao4423.twitter4j.Status
import sugtao4423.twitterweb4j.model.CreateTweet
import kotlin.math.round

data class TweetUiState(
    val accountScreenName: String = "",
    val actionBarTitle: Int? = null,

    val tweetText: String = "",
    val prefixLength: Int = 0,

    val selectedMedia: Uri? = null,
) {
    private val parsed = TwitterTextParser.parseTweet(tweetText)
    val remainingTextCount: Int = 140 - parsed.weightedLength.let {
        if (it % 2 == 0) it / 2 else (it + 1) / 2
    }
    val isValidTextCount: Boolean = parsed.isValid || remainingTextCount == 140
}

sealed interface TweetEvent {
    data object Finish : TweetEvent
    data class ShowOriginStatus(val status: Status) : TweetEvent
    data object SetTextSelectionEnd : TweetEvent
}

class TweetActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    val fontAwesomeTypeface = app.fontAwesomeTypeface

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

    private val _uiState = MutableStateFlow(
        TweetUiState(accountScreenName = "@${app.account.screenName}")
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<TweetEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private fun onSetTweetType() {
        val actionBarTitle = when (tweetType) {
            TweetActivity.TYPE_REPLY, TweetActivity.TYPE_QUOTERT -> null
            TweetActivity.TYPE_UNOFFICIALRT -> R.string.unofficial_rt
            else -> R.string.new_tweet
        }
        _uiState.update { it.copy(actionBarTitle = actionBarTitle) }

        when (tweetType) {
            TweetActivity.TYPE_REPLY, TweetActivity.TYPE_QUOTERT ->
                _events.trySend(TweetEvent.ShowOriginStatus(toStatus!!))
        }

        when (tweetType) {
            TweetActivity.TYPE_REPLY -> {
                val mentionUsers =
                    setOf(toStatus!!.user.screenName) + toStatus!!.userMentionEntities.filter {
                        it.id != app.account.id
                    }.map { it.screenName }.toSet()
                val replyUserScreenNames = mentionUsers.joinToString(" @", "@") + " "
                _uiState.update {
                    it.copy(
                        tweetText = replyUserScreenNames,
                        prefixLength = replyUserScreenNames.length,
                    )
                }
            }

            TweetActivity.TYPE_UNOFFICIALRT -> {
                val unOfficial = " RT @${toStatus!!.user.screenName}: ${toStatus!!.text}"
                _uiState.update { it.copy(tweetText = unOfficial) }
            }

            TweetActivity.TYPE_PAKUTSUI -> {
                _uiState.update { it.copy(tweetText = toStatus!!.text) }
                _events.trySend(TweetEvent.SetTextSelectionEnd)
            }

            TweetActivity.TYPE_EXTERNALTEXT -> {
                _uiState.update { it.copy(tweetText = externalText ?: "") }
                _events.trySend(TweetEvent.SetTextSelectionEnd)
            }
        }
    }

    fun onTweetTextChanged(string: String) = _uiState.update {
        it.copy(tweetText = string)
    }

    fun clickClose() = _events.trySend(TweetEvent.Finish)

    fun clickTweet() {
        val text = _uiState.value.tweetText.substring(_uiState.value.prefixLength)
        val createTweet = CreateTweet(text)

        when (tweetType) {
            TweetActivity.TYPE_REPLY -> createTweet.inReplyToStatusId = toStatus!!.id
            TweetActivity.TYPE_QUOTERT -> createTweet.attachmentUrl = toStatus!!.toStatusUrl()
        }
        app.updateStatus(createTweet, _uiState.value.selectedMedia)

        _events.trySend(TweetEvent.Finish)
    }

    fun onMediaPicked(uri: Uri?) {
        if (uri == null) return

        canUploadMedia(uri)?.let {
            app.showToast(it)
            return
        }
        _uiState.update { it.copy(selectedMedia = uri) }
        app.showToast(R.string.success_select_media)
    }

    fun onSpeeched(result: ActivityResult?) {
        if (result?.resultCode != Activity.RESULT_OK || result.data == null) return

        val results =
            result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: arrayListOf("")
        _uiState.update { it.copy(tweetText = it.tweetText + results[0]) }
        _events.trySend(TweetEvent.SetTextSelectionEnd)
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

        _uiState.update { it.copy(tweetText = it.tweetText + str) }
        _events.trySend(TweetEvent.SetTextSelectionEnd)
    }

    fun textOptionOmatase() {
        _uiState.update {
            it.copy(tweetText = it.tweetText.toCharArray().joinToString("　"))
        }
        _events.trySend(TweetEvent.SetTextSelectionEnd)
    }

    fun textOptionTotsuzenNoShi() {
        fun stringSize(text: String): Double = text.toCharArray().fold(0.0) { buf, value ->
            buf + if (value.toString().toByteArray().size <= 1) .5 else 1.0
        }

        val lines = _uiState.value.tweetText.split("\n")
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

        _uiState.update { it.copy(tweetText = dead) }
        _events.trySend(TweetEvent.SetTextSelectionEnd)
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
