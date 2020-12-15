package sugtao4423.lod.dialog_onclick

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import twitter4j.Status
import twitter4j.TwitterException

class Dialog_talk(status: Status, private val context: Context, private val dialog: AlertDialog) : View.OnClickListener {

    private var reply = if (status.isRetweet) status.retweetedStatus else status
    private val resultAdapter = TweetListAdapter(context)

    override fun onClick(v: View?) {
        dialog.dismiss()
        val result = TweetListView(context)
        result.adapter = resultAdapter
        AlertDialog.Builder(context).setView(result).show()

        resultAdapter.add(reply)

        loadConversation()
    }

    private fun loadConversation() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    reply = (context.applicationContext as App).getTwitter().showStatus(reply.inReplyToStatusId)
                    reply
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                resultAdapter.add(result)
                if (result.inReplyToStatusId > 0) {
                    loadConversation()
                } else {
                    ShowToast(context.applicationContext, R.string.success_get_talk_list)
                }
            } else {
                ShowToast(context.applicationContext, R.string.error_get_talk_list)
            }
        }
    }

}