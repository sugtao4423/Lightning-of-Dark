package sugtao4423.lod.dialog_onclick

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import sugtao4423.lod.ui.tweet.TweetActivity
import twitter4j.Status

class Dialog_unOfficialRT(private val status: Status, private val context: Context, private val dialog: AlertDialog) : View.OnClickListener {

    override fun onClick(v: View?) {
        dialog.dismiss()
        val i = Intent(context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_UNOFFICIALRT)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status)
        }
        context.startActivity(i)
    }

}
