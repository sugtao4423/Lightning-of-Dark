package sugtao4423.lod.ui.main.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ui.BaseTweetListViewModel
import sugtao4423.lod.utils.showToast

class HomeFragmentViewModel(application: Application) : BaseTweetListViewModel(application) {

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                if (app.account.listAsTL > 0) {
                    app.twitter.listTweetsTimeline(app.account.listAsTL, tweetCount, bottomCursor)
                } else {
                    app.twitter.homeLatestTimeline(tweetCount, bottomCursor)
                }
            }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_timeline)
            return@launch
        }

        if (result.isNotEmpty()) {
            bottomCursor = result.cursorBottom
            if (isRefresh) {
                app.cursorTop = result.cursorTop
            }
        }
        hasNextPage = result.isNotEmpty()
        result.let { addStatuses.value = it }
    }

}
