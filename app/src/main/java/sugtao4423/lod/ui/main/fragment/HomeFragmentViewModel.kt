package sugtao4423.lod.ui.main.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ui.BaseTweetListViewModel
import sugtao4423.lod.utils.showToast
import twitter4j.Paging

class HomeFragmentViewModel(application: Application) : BaseTweetListViewModel(application) {

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        val paging = Paging(1, 50).let {
            if (maxId > 0) it.maxId(maxId) else it
        }
        val result = withContext(Dispatchers.IO) {
            runCatching {
                if (app.account.listAsTL > 0) {
                    app.twitter.getUserListStatuses(app.account.listAsTL, paging)
                } else {
                    app.twitter.getHomeTimeline(paging)
                }
            }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_timeline)
            return@launch
        }

        if (result.isNotEmpty()) {
            maxId = result.last().id - 1
            if (isRefresh) {
                app.latestTweetId = result.first().id
            }
        }
        hasNextPage = result.isNotEmpty()
        result.let { addStatuses.value = it }
    }

}
