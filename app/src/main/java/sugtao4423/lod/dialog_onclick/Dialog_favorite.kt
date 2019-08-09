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

class Dialog_favorite(private val status: Status, private val context: Context, private val dialog: AlertDialog) : View.OnClickListener {

    override fun onClick(v: View?) {
        dialog.dismiss()
        if (status.isFavorited) {
            AlertDialog.Builder(context).apply {
                setMessage(R.string.is_unfavorite)
                setNegativeButton(R.string.cancel, null)
                setPositiveButton(R.string.ok) { _, _ ->
                    Favorite(true).execute()
                }
                show()
            }
        } else {
            Favorite(false).execute()
        }
    }

    inner class Favorite(private val isUnfavorite: Boolean) : AsyncTask<Unit, Unit, Status?>() {

        override fun doInBackground(vararg params: Unit?): twitter4j.Status? {
            val twitter = (context.applicationContext as App).getTwitter()
            return try {
                if (isUnfavorite) {
                    twitter.destroyFavorite(this@Dialog_favorite.status.id)
                } else {
                    twitter.createFavorite(this@Dialog_favorite.status.id)
                }
            } catch (e: TwitterException) {
                null
            }
        }

        override fun onPostExecute(result: twitter4j.Status?) {
            val toastMessage = when {
                result != null && isUnfavorite -> R.string.success_unfavorite
                result != null && !isUnfavorite -> R.string.success_favorite
                result == null && isUnfavorite -> R.string.error_unfavorite
                result == null && !isUnfavorite -> R.string.error_favorite
                else -> -1
            }
            ShowToast(context.applicationContext, toastMessage)
        }

    }

}