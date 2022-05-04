package sugtao4423.lod.ui.adapter.tweet.click.listener

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.showToast
import twitter4j.Status

class RetweetListener(
    private val status: Status,
    private val context: Context,
    private val onClicked: () -> Unit,
) : View.OnClickListener {

    override fun onClick(v: View?) {
        onClicked()

        if (!status.isRetweeted) {
            retweet(false)
            return
        }

        AlertDialog.Builder(context).apply {
            setMessage(R.string.is_unretweet)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ -> retweet(true) }
            show()
        }
    }

    private fun retweet(isUnRetweet: Boolean) {
        val twitter = (context.applicationContext as App).twitter
        CoroutineScope(Dispatchers.Main).launch {
            val result = runCatching {
                if (isUnRetweet) {
                    twitter.unRetweetStatus(status.id)
                } else {
                    twitter.retweetStatus(status.id)
                }
            }.getOrNull()
            val toastMessage = when {
                result != null && !isUnRetweet -> R.string.success_retweet
                result != null && isUnRetweet -> R.string.success_unretweet
                result == null && !isUnRetweet -> R.string.error_retweet
                result == null && isUnRetweet -> R.string.error_unretweet
                else -> -1
            }
            context.showToast(toastMessage)
        }
    }

}
