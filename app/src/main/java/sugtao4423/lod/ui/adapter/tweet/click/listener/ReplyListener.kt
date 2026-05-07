package sugtao4423.lod.ui.adapter.tweet.click.listener

import android.content.Context
import android.content.Intent
import android.view.View
import sugtao4423.lod.ui.tweet.TweetActivity
import twitter4j.Status

class ReplyListener(
    private val status: Status,
    private val context: Context,
    private val onClicked: () -> Unit,
) : View.OnClickListener {

    override fun onClick(v: View?) {
        onClicked()

        val reply = Intent(context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_REPLY)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status)
        }
        context.startActivity(reply)
    }

}
