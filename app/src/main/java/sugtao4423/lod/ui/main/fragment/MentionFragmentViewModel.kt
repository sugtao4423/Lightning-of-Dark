package sugtao4423.lod.ui.main.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ui.BaseListEvent
import sugtao4423.lod.ui.TweetListViewModel
import sugtao4423.lod.utils.showToast

class MentionFragmentViewModel(application: Application) : TweetListViewModel(application) {

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.mentionsTimeline(tweetCount, bottomCursor) }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_mention)
            return@launch
        }

        if (result.isNotEmpty()) {
            bottomCursor = result.cursorBottom
        }
        hasNextPage = result.isNotEmpty()
        _events.trySend(BaseListEvent.AddItems(result))
    }

}
