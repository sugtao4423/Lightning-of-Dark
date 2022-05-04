package sugtao4423.lod.ui.adapter.tweet.click.listener

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.showToast
import twitter4j.Status

class DeleteTweetListener(
    private val status: Status,
    private val context: Context,
    private val onClicked: () -> Unit,
) : View.OnClickListener {

    override fun onClick(v: View?) {
        onClicked()

        AlertDialog.Builder(context).apply {
            setMessage(R.string.is_post_delete)
            setNegativeButton(R.string.no, null)
            setPositiveButton(R.string.yes) { _, _ -> deleteTweet() }
            show()
        }
    }

    private fun deleteTweet() {
        val twitter = (context.applicationContext as App).twitter
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { twitter.destroyStatus(status.id) }.getOrNull()
            }
            val message = if (result == null) {
                R.string.error_post_delete
            } else {
                R.string.success_post_delete
            }
            context.showToast(message)
        }
    }

}
