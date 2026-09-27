package sugtao4423.lod.ui.intent

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.Regex
import sugtao4423.lod.utils.showToast
import sugtao4423.twitter4j.Status

sealed interface IntentEvent {
    data class StartTweetActivity(val text: String) : IntentEvent
    data class StartUserPageActivity(val screenName: String) : IntentEvent
    data class ShowStatusDialog(val status: Status) : IntentEvent
}

class IntentActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    val hasAccount = app.hasAccount

    private val _events = Channel<IntentEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun doIntentAction(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            onActionView(intent.data!!)
        } else if (intent.action == Intent.ACTION_SEND && intent.extras != null) {
            onActionSend(intent.extras!!)
        }
    }

    private fun onActionView(intentData: Uri) {
        val uri = intentData.toString()
        val matchStatus = Regex.statusUrl.matcher(uri)
        val matchShare = Regex.shareUrl.matcher(uri)
        val matchUser = Regex.userUrl.matcher(uri)

        when {
            matchStatus.find() -> {
                val id = matchStatus.group(Regex.statusUrlStatusIdGroup)!!.toLong()
                showStatus(id)
            }

            matchShare.find() -> onActionViewShare(intentData)
            matchUser.find() -> {
                val screenName = matchUser.group(Regex.userUrlScreenNameGroup)!!
                _events.trySend(IntentEvent.StartUserPageActivity(screenName))
            }
        }
    }

    private fun onActionViewShare(shareUri: Uri) {
        val map = shareUri.queryParameterNames.associateWith {
            shareUri.getQueryParameter(it) ?: ""
        }
        val text = arrayListOf<String>().apply {
            map["text"]?.let { add(it) }
            map["url"]?.let { add(it) }
            map["hashtags"]?.let {
                val str = "#" + it.replace(",", " #")
                add(str)
            }
            map["via"]?.let {
                val str = "@${it}さんから"
                add(str)
            }
        }.joinToString(" ")
        _events.trySend(IntentEvent.StartTweetActivity(text))
    }

    private fun onActionSend(intentExtra: Bundle) {
        val subject = intentExtra.getString(Intent.EXTRA_SUBJECT) ?: ""
        val text = intentExtra.getString(Intent.EXTRA_TEXT) ?: ""

        val youtubeShareMatcher = Regex.youtubeShareSubject.matcher(subject)
        val tweetText = when {
            youtubeShareMatcher.find() -> {
                "${youtubeShareMatcher.group(Regex.youtubeVideoNameGroup)} $text @YouTubeより"
            }

            subject.isEmpty() -> text
            else -> "$subject $text"
        }
        _events.trySend(IntentEvent.StartTweetActivity(tweetText))
    }

    fun showStatus(status: Status) {
        _events.trySend(IntentEvent.ShowStatusDialog(status))
    }

    fun showStatus(statusId: Long) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.tweetDetail(statusId) }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_status)
            return@launch
        }

        _events.trySend(IntentEvent.ShowStatusDialog(result))
    }

}
