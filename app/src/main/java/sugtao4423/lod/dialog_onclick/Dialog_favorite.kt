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
import sugtao4423.lod.utils.showToast
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
                    favorite(false)
                }
                show()
            }
        } else {
            favorite(true)
        }
    }

    private fun favorite(isFavorite: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                val twitter = (context.applicationContext as App).twitter
                try {
                    if (isFavorite) {
                        twitter.createFavorite(this@Dialog_favorite.status.id)
                    } else {
                        twitter.destroyFavorite(this@Dialog_favorite.status.id)
                    }
                } catch (e: TwitterException) {
                    null
                }
            }
            val toastMessage = when {
                result != null && isFavorite -> R.string.success_favorite
                result != null && !isFavorite -> R.string.success_unfavorite
                result == null && isFavorite -> R.string.error_favorite
                result == null && !isFavorite -> R.string.error_unfavorite
                else -> -1
            }
            context.showToast(toastMessage)
        }
    }

}
