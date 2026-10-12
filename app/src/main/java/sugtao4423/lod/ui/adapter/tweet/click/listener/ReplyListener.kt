package sugtao4423.lod.ui.adapter.tweet.click.listener

import android.content.Intent
import android.view.View
import sugtao4423.lod.ui.tweet.TweetActivity
import sugtao4423.twitter4j.Status

class ReplyListener(
    private val status: Status,
    private val onClicked: () -> Unit,
) : View.OnClickListener {

    override fun onClick(view: View) {
        onClicked()

        val reply = Intent(view.context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_REPLY)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status)
        }
        view.context.startActivity(reply)
    }

}
