package sugtao4423.lod.dialog_onclick

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.ShowToast
import twitter4j.Status
import twitter4j.TwitterException

class Dialog_deletePost(private val status: Status, private val context: Context, private val dialog: AlertDialog) : View.OnClickListener {

    override fun onClick(v: View?) {
        dialog.dismiss()
        AlertDialog.Builder(context).apply {
            setMessage(R.string.is_post_delete)
            setNegativeButton(R.string.no, null)
            setPositiveButton(R.string.yes) { _, _ ->
                deletePost()
            }
            show()
        }
    }

    private fun deletePost() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    (context.applicationContext as App).twitter.destroyStatus(this@Dialog_deletePost.status.id)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                ShowToast(context.applicationContext, R.string.success_post_delete)
            } else {
                ShowToast(context.applicationContext, R.string.error_post_delete)
            }
        }
    }

}
