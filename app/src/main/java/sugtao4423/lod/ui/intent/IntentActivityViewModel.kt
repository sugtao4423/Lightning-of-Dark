package sugtao4423.lod.ui.intent

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.ShowToast
import sugtao4423.lod.utils.Regex
import twitter4j.Status

class IntentActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    val hasAccount = app.hasAccount

    private val _onStartTweetActivity = LiveEvent<String>()
    val onStartTweetActivity: LiveData<String> = _onStartTweetActivity

    private val _onStartUserPageActivity = LiveEvent<String>()
    val onStartUserPageActivity: LiveData<String> = _onStartUserPageActivity

    private val _showStatusDialog = LiveEvent<Status>()
    val showStatusDialog: LiveData<Status> = _showStatusDialog

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
                _onStartUserPageActivity.value = screenName
            }
        }
    }

    private fun onActionViewShare(shareUri: Uri) {
        val map = HashMap<String, String>()
        shareUri.queryParameterNames.map {
            map.put(it, shareUri.getQueryParameter(it) ?: "")
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
        _onStartTweetActivity.value = text
    }

    private fun onActionSend(intentExtra: Bundle) {
        val subject = intentExtra.getString(Intent.EXTRA_SUBJECT)
        val text = intentExtra.getString(Intent.EXTRA_TEXT)
        val tweetText = if (subject.isNullOrBlank()) text!! else "$subject $text"
        _onStartTweetActivity.value = tweetText
    }

    fun showStatus(tweetId: Long) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.showStatus(tweetId) }.getOrNull()
        }
        if (result == null) {
            ShowToast(app, R.string.error_get_status)
            return@launch
        }

        result.let { _showStatusDialog.value = it }
    }

}
