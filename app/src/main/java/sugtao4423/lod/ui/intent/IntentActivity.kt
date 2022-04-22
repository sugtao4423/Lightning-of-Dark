package sugtao4423.lod.ui.intent

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import sugtao4423.lod.TweetActivity
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import sugtao4423.lod.ui.addaccount.AddAccountActivity
import sugtao4423.lod.ui.userpage.UserPageActivity
import twitter4j.Status

class IntentActivity : AppCompatActivity() {

    companion object {
        const val TWEET_ID = "tweetId"
    }

    private val viewModel: IntentActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!viewModel.hasAccount) {
            startActivity(Intent(this, AddAccountActivity::class.java))
            finish()
            return
        }

        viewModel.onStartTweetActivity.observe(this) {
            val i = Intent(this, TweetActivity::class.java).apply {
                putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_EXTERNALTEXT)
                putExtra(TweetActivity.INTENT_EXTRA_KEY_TEXT, it)
            }
            startActivity(i)
            finish()
        }
        viewModel.onStartUserPageActivity.observe(this) {
            val i = Intent(this, UserPageActivity::class.java)
            i.putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_SCREEN_NAME, it)
            startActivity(i)
            finish()
        }
        viewModel.showStatusDialog.observe(this) {
            showStatusDialog(it)
        }

        val tweetId = intent.getLongExtra(TWEET_ID, -1)
        if (tweetId == -1L) {
            viewModel.doIntentAction(intent)
        } else {
            viewModel.showStatus(tweetId)
        }
    }

    private fun showStatusDialog(status: Status) {
        val tweetListView = TweetListView(this)
        TweetListAdapter(this).also {
            it.add(status)
            tweetListView.adapter = it
        }
        AlertDialog.Builder(this).apply {
            setView(tweetListView)
            setOnDismissListener { finish() }
            window.setDimAmount(0f)
            show()
        }
    }

}
