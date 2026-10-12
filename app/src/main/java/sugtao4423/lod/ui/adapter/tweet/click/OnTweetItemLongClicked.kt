package sugtao4423.lod.ui.adapter.tweet.click

import android.content.Intent
import android.view.View
import sugtao4423.lod.ui.tweet.TweetActivity
import sugtao4423.twitter4j.Status

object OnTweetItemLongClicked {

    fun onItemLongClicked(view: View, status: Status) {
        val i = Intent(view.context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_PAKUTSUI)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status)
        }
        view.context.startActivity(i)
    }

}
