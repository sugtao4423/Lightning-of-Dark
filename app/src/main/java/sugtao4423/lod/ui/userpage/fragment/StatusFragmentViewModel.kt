package sugtao4423.lod.ui.userpage.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ui.BaseTweetListViewModel
import sugtao4423.lod.utils.showToast
import sugtao4423.twitterweb4j.model.CursorList
import twitter4j.Status
import twitter4j.User

class StatusFragmentViewModel(application: Application) : BaseTweetListViewModel(application) {

    var user: User? = null
    var fragmentType: String = StatusFragment.TYPE_TWEET

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        if (user == null) return@launch

        val result = withContext(Dispatchers.IO) {
            runCatching { getStatuses() }.getOrNull()
        }
        if (result == null) {
            app.showToast(getErrorToastStringRes())
            return@launch
        }

        if (result.isNotEmpty()) {
            bottomCursor = result.cursorBottom
        }
        hasNextPage = result.isNotEmpty()
        addStatuses.value = result
    }

    private fun getStatuses(): CursorList<Status> = when (fragmentType) {
        StatusFragment.TYPE_TWEET -> app.twitter.userTweetsAndReplies(
            user!!.id, tweetCount, bottomCursor
        )

        StatusFragment.TYPE_FAVORITE -> app.twitter.favorites(user!!.id, tweetCount, bottomCursor)

        else -> throw UnsupportedOperationException()
    }

    private fun getErrorToastStringRes(): Int = when (fragmentType) {
        StatusFragment.TYPE_TWEET -> R.string.error_get_timeline
        StatusFragment.TYPE_FAVORITE -> R.string.error_get_favorite
        else -> throw UnsupportedOperationException()
    }

}
