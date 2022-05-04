package sugtao4423.lod.ui.adapter.tweet.click.listener

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.utils.showToast
import sugtao4423.lod.view.TweetListView
import twitter4j.Status

class TalkListener(
    private val status: Status,
    private val context: Context,
    private val onClicked: () -> Unit,
) : View.OnClickListener {

    private val twitter = (context.applicationContext as App).twitter
    private val talkAdapter = TweetListAdapter(context)

    override fun onClick(v: View?) {
        onClicked()

        val tweetListView = TweetListView(context).apply {
            adapter = talkAdapter
        }
        AlertDialog.Builder(context).setView(tweetListView).show()

        talkAdapter.add(status)
        loadConversation(status)
    }

    private fun loadConversation(toLoadTalkStatus: Status) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { twitter.showStatus(toLoadTalkStatus.inReplyToStatusId) }.getOrNull()
            }
            if (result == null) {
                context.showToast(R.string.error_get_talk_list)
                return@launch
            }

            talkAdapter.add(result)
            if (result.inReplyToStatusId > 0) {
                loadConversation(result)
            } else {
                context.showToast(R.string.success_get_talk_list)
            }
        }
    }

}
