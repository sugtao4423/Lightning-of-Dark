package sugtao4423.lod.dialog_onclick

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.View
import sugtao4423.lod.TweetActivity
import twitter4j.Status

class Dialog_quoteRT(private val status: Status, private val context: Context, private val dialog: AlertDialog) : View.OnLongClickListener {

    override fun onLongClick(v: View?): Boolean {
        dialog.dismiss()
        val i = Intent(context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_QUOTERT)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status)
        }
        context.startActivity(i)
        return true
    }

}