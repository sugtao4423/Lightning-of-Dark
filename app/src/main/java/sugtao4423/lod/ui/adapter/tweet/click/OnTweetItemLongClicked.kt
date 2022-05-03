package sugtao4423.lod.ui.adapter.tweet.click

import android.content.Intent
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.tweet.TweetActivity

class OnTweetItemLongClicked(private val tweetListAdapter: TweetListAdapter) {

    fun onItemLongClicked(position: Int) {
        val status = tweetListAdapter.data[position]
        val i = Intent(tweetListAdapter.context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_PAKUTSUI)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status)
        }
        tweetListAdapter.context.startActivity(i)
    }

}
