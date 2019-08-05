package sugtao4423.lod.dialog_onclick

import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.view.View
import sugtao4423.lod.App
import sugtao4423.lod.ListViewListener
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
        resultAdapter.let {
            it.onItemClickListener = ListViewListener()
            it.onItemLongClickListener = ListViewListener()
            result.adapter = it
        }
        AlertDialog.Builder(context).setView(result).show()

        resultAdapter.add(reply)

        LoadConversation().execute()
    }

    private inner class LoadConversation : AsyncTask<Unit, Unit, Status?>() {

        override fun doInBackground(vararg params: Unit?): twitter4j.Status? {
            return try {
                reply = (context.applicationContext as App).getTwitter().showStatus(reply.inReplyToStatusId)
                reply
            } catch (e: TwitterException) {
                null
            }
        }

        override fun onPostExecute(result: twitter4j.Status?) {
            if (result != null) {
                resultAdapter.add(result)
                if (result.inReplyToStatusId > 0) {
                    LoadConversation().execute()
                } else {
                    ShowToast(context.applicationContext, R.string.success_get_talk_list)
                }
            } else {
                ShowToast(context.applicationContext, R.string.error_get_talk_list)
            }
        }

    }

}