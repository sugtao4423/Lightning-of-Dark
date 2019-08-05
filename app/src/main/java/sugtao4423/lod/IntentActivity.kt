package sugtao4423.lod

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import sugtao4423.lod.userpage_fragment.UserPage
import sugtao4423.lod.utils.Regex
import twitter4j.Status
import twitter4j.TwitterException

class IntentActivity : AppCompatActivity() {

    companion object {
        const val TWEET_ID = "tweetId"
    }

    private lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = applicationContext as App

        if (!app.haveAccount()) {
            startActivity(Intent(this, StartOAuth::class.java))
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
                    showStatus(status.group(Regex.statusUrlStatusIdGroup).toLong())
                }
                share.find() -> {
                    val shareUri = Uri.parse(uri)
                    val map = HashMap<String, String>()
                    shareUri.queryParameterNames.map {
                        map.put(it, shareUri.getQueryParameter(it) ?: "")
                    }
                    val text = arrayListOf<String>().apply {
                        if (map["text"] != null) {
                            add(map["text"]!!)
                        }
                        if (map["url"] != null) {
                            add(map["url"]!!)
                        }
                        if (map["hashtags"] != null) {
                            val str = "#" + map["hashtags"]!!.replace(",", " #")
                            add(str)
                        }
                        if (map["via"] != null) {
                            val str = "@" + map["via"]!! + "さんから"
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
                    val i = Intent(this, UserPage::class.java)
                    i.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, user.group(Regex.userUrlScreenNameGroup))
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
        object : AsyncTask<Long, Unit, Status?>() {

            override fun doInBackground(vararg params: Long?): twitter4j.Status? {
                return try {
                    app.getTwitter().showStatus(tweetId)
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: twitter4j.Status?) {
                if (result != null) {
                    val l = TweetListView(this@IntentActivity)
                    val adapter = TweetListAdapter(this@IntentActivity)
                    adapter.add(result)
                    l.adapter = adapter
                    adapter.onItemClickListener = ListViewListener()
                    adapter.onItemLongClickListener = ListViewListener()
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
        }.execute()
    }

}