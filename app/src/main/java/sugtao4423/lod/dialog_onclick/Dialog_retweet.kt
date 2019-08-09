package sugtao4423.lod.dialog_onclick

import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.view.View
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
                    Retweet(true).execute()
                }
                show()
            }
        } else {
            Retweet(false).execute()
        }
    }

    inner class Retweet(private val isUnretweet: Boolean) : AsyncTask<Unit, Unit, Status?>() {

        override fun doInBackground(vararg params: Unit?): twitter4j.Status? {
            val twitter = (context.applicationContext as App).getTwitter()
            return try {
                if (isUnretweet) {
                    twitter.unRetweetStatus(this@Dialog_retweet.status.id)
                } else {
                    twitter.retweetStatus(this@Dialog_retweet.status.id)
                }
            } catch (e: TwitterException) {
                null
            }
        }

        override fun onPostExecute(result: twitter4j.Status?) {
            val toastMessage = when {
                result != null && isUnretweet -> R.string.success_unretweet
                result != null && !isUnretweet -> R.string.success_retweet
                result == null && isUnretweet -> R.string.error_unretweet
                result == null && !isUnretweet -> R.string.error_retweet
                else -> -1
            }
            ShowToast(context.applicationContext, toastMessage)
        }

    }

}