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
        object : AsyncTask<Unit, Unit, Boolean>() {
            override fun doInBackground(vararg params: Unit?): Boolean {
                return try {
                    (context.applicationContext as App).getTwitter().destroyStatus(this@Dialog_deletePost.status.id)
                    true
                } catch (e: TwitterException) {
                    false
                }
            }

            override fun onPostExecute(result: Boolean) {
                if (result) {
                    ShowToast(context.applicationContext, R.string.success_post_delete)
                } else {
                    ShowToast(context.applicationContext, R.string.error_post_delete)
                }
            }
        }.execute()
    }

}