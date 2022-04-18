package sugtao4423.lod.ui.main.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.ui.BaseTweetListViewModel
import twitter4j.Paging

class MentionFragmentViewModel(application: Application) : BaseTweetListViewModel(application) {

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        val paging = Paging(1, 50).let {
            if (maxId > 0) it.maxId(maxId) else it
        }
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.getMentionsTimeline(paging) }.getOrNull()
        }
        if (result == null) {
            ShowToast(app, R.string.error_get_mention)
            return@launch
        }

        if (result.isNotEmpty()) {
            maxId = result.last().id - 1
        }
        hasNextPage = result.isNotEmpty()
        addStatuses.value = result
    }

}
