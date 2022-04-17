package sugtao4423.lod.dialog_onclick

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import sugtao4423.icondialog.IconDialog
import sugtao4423.icondialog.IconItem
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.TweetActivity
import twitter4j.Status

class Dialog_reply(private val status: Status, private val context: Context, private val dialog: AlertDialog) : View.OnClickListener {

    private val myScreenName = (context.applicationContext as App).account.screenName

    override fun onClick(v: View?) {
        dialog.dismiss()

        val item = if (status.isRetweet) status.retweetedStatus else status

        val mentions = arrayListOf<String>()
        mentions.add(item.user.screenName)
        item.userMentionEntities.map {
            if (it.screenName != myScreenName && !mentions.contains(it.screenName)) {
                mentions.add(it.screenName)
            }
        }

        if (mentions.size > 1) {
            selectReplyDialog()
        } else {
            reply()
        }
    }

    private fun selectReplyDialog() {
        val black = ContextCompat.getColor(context, R.color.icon)
        val items = arrayOf(
                IconItem(context.getString(R.string.icon_reply)[0], black, context.getString(R.string.reply)),
                IconItem(context.getString(R.string.icon_replyAll)[0], black, context.getString(R.string.reply_all))
        )
        IconDialog(context).apply {
            setItems(items) { _, which ->
                when (which) {
                    0 -> reply()
                    1 -> replyAll()
                }
            }
            show()
        }
    }

    private fun reply() {
        val reply = Intent(context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_REPLY)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status)
        }
        context.startActivity(reply)
    }

    private fun replyAll() {
        val reply = Intent(context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_REPLYALL)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status)
        }
        context.startActivity(reply)
    }

}
