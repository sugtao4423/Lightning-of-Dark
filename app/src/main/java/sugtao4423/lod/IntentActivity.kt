package sugtao4423.lod

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import sugtao4423.lod.ui.addaccount.AddAccountActivity
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.utils.Regex
import twitter4j.TwitterException

class IntentActivity : AppCompatActivity() {

    companion object {
        const val TWEET_ID = "tweetId"
    }

    private lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = applicationContext as App

        if (!app.hasAccount) {
            startActivity(Intent(this, AddAccountActivity::class.java))
            finish()
            return
        }

        val tweetId = intent.getLongExtra(TWEET_ID, -1)
        if (tweetId == -1L) {
            jump()
        } else {
            showStatus(tweetId)
        }
    }

    private fun jump() {
        if (intent.action == Intent.ACTION_VIEW) {
            if (intent.data == null) {
                return
            }

            val uri = intent.data!!.toString()
            val status = Regex.statusUrl.matcher(uri)
            val share = Regex.shareUrl.matcher(uri)
            val user = Regex.userUrl.matcher(uri)
            when {
                status.find() -> {
                    showStatus(status.group(Regex.statusUrlStatusIdGroup)!!.toLong())
                }
                share.find() -> {
                    val shareUri = Uri.parse(uri)
                    val map = HashMap<String, String>()
                    shareUri.queryParameterNames.map {
                        map.put(it, shareUri.getQueryParameter(it) ?: "")
                    }
                    val text = arrayListOf<String>().apply {
                        map["text"]?.let {
                            add(it)
                        }
                        map["url"]?.let {
                            add(it)
                        }
                        map["hashtags"]?.let {
                            val str = "#" + it.replace(",", " #")
                            add(str)
                        }
                        map["via"]?.let {
                            val str = "@${it}さんから"
                            add(str)
                        }
                    }.joinToString(" ")
                    val i = Intent(this, TweetActivity::class.java).apply {
                        putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_EXTERNALTEXT)
                        putExtra(TweetActivity.INTENT_EXTRA_KEY_TEXT, text)
                    }
                    startActivity(i)
                    finish()
                }
                user.find() -> {
                    val i = Intent(this, UserPageActivity::class.java)
                    i.putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_SCREEN_NAME, user.group(Regex.userUrlScreenNameGroup))
                    startActivity(i)
                    finish()
                }
            }
        } else if (intent.action == Intent.ACTION_SEND) {
            if (intent.extras == null) {
                return
            }

            val subject = intent.extras!!.getString(Intent.EXTRA_SUBJECT)
            val text = intent.extras!!.getString(Intent.EXTRA_TEXT)
            val tweetText = if (subject.isNullOrBlank()) {
                text
            } else {
                "$subject $text"
            }
            val i = Intent(this, TweetActivity::class.java).apply {
                putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_EXTERNALTEXT)
                putExtra(TweetActivity.INTENT_EXTRA_KEY_TEXT, tweetText)
            }
            startActivity(i)
            finish()
        }
    }

    private fun showStatus(tweetId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    app.twitter.showStatus(tweetId)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                val l = TweetListView(this@IntentActivity)
                val adapter = TweetListAdapter(this@IntentActivity)
                adapter.add(result)
                l.adapter = adapter
                AlertDialog.Builder(this@IntentActivity).apply {
                    setView(l)
                    setOnDismissListener {
                        finish()
                    }
                    window.setDimAmount(0f)
                    show()
                }
            } else {
                ShowToast(applicationContext, R.string.error_get_status)
            }
        }
    }

}
