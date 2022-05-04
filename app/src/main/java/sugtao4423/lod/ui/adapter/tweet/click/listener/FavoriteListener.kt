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

class FavoriteListener(
    private val status: Status,
    private val context: Context,
    private val onClicked: () -> Unit,
) : View.OnClickListener {

    override fun onClick(v: View?) {
        onClicked()

        if (!status.isFavorited) {
            favorite(false)
            return
        }

        AlertDialog.Builder(context).apply {
            setMessage(R.string.is_unfavorite)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ -> favorite(true) }
            show()
        }
    }

    private fun favorite(isUnFav: Boolean) {
        val twitter = (context.applicationContext as App).twitter
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (isUnFav) {
                        twitter.destroyFavorite(status.id)
                    } else {
                        twitter.createFavorite(status.id)
                    }
                }.getOrNull()
            }
            val toastMessage = when {
                result != null && !isUnFav -> R.string.success_favorite
                result != null && isUnFav -> R.string.success_unfavorite
                result == null && !isUnFav -> R.string.error_favorite
                result == null && isUnFav -> R.string.error_unfavorite
                else -> throw UnsupportedOperationException()
            }
            context.showToast(toastMessage)
        }
    }

}
