package sugtao4423.lod.dialog_onclick

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.Status
import twitter4j.TwitterException

class Dialog_retweet(private val status: Status, private val context: Context, private val dialog: AlertDialog) : View.OnClickListener {

    override fun onClick(v: View?) {
        dialog.dismiss()
        if (status.isRetweeted) {
            AlertDialog.Builder(context).apply {
                setMessage(R.string.is_unretweet)
                setNegativeButton(R.string.cancel, null)
                setPositiveButton(R.string.ok) { _, _ ->
                    retweet(false)
                }
                show()
            }
        } else {
            retweet(true)
        }
    }

    private fun retweet(isRetweet: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                val twitter = (context.applicationContext as App).getTwitter()
                try {
                    if (isRetweet) {
                        twitter.retweetStatus(this@Dialog_retweet.status.id)
                    } else {
                        twitter.unRetweetStatus(this@Dialog_retweet.status.id)
                    }
                } catch (e: TwitterException) {
                    null
                }
            }
            val toastMessage = when {
                result != null && isRetweet -> R.string.success_retweet
                result != null && !isRetweet -> R.string.success_unretweet
                result == null && isRetweet -> R.string.error_retweet
                result == null && !isRetweet -> R.string.error_unretweet
                else -> -1
            }
            ShowToast(context.applicationContext, toastMessage)
        }
    }

}