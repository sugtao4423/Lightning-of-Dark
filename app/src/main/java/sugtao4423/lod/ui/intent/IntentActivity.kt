package sugtao4423.lod.ui.intent

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.addaccount.AddAccountActivity
import sugtao4423.lod.ui.tweet.TweetActivity
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.view.TweetListView
import sugtao4423.twitter4j.Status

class IntentActivity : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_STATUS = "status"
        const val INTENT_EXTRA_KEY_STATUS_ID = "statusId"
    }

    private val viewModel: IntentActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!viewModel.hasAccount) {
            startActivity(Intent(this, AddAccountActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect(::handleEvent)
            }
        }

        val status = intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS) as? Status
        val statusId = intent.getLongExtra(INTENT_EXTRA_KEY_STATUS_ID, -1)
        when {
            status != null -> viewModel.showStatus(status)
            statusId != -1L -> viewModel.showStatus(statusId)
            else -> viewModel.doIntentAction(intent)
        }
    }

    private fun handleEvent(event: IntentEvent) = when (event) {
        is IntentEvent.StartTweetActivity -> {
            val i = Intent(this, TweetActivity::class.java).apply {
                putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_EXTERNALTEXT)
                putExtra(TweetActivity.INTENT_EXTRA_KEY_TEXT, event.text)
            }
            startActivity(i)
            finish()
        }

        is IntentEvent.StartUserPageActivity -> {
            val i = Intent(this, UserPageActivity::class.java)
            i.putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_SCREEN_NAME, event.screenName)
            startActivity(i)
            finish()
        }

        is IntentEvent.ShowStatusDialog -> showStatusDialog(event.status)
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
