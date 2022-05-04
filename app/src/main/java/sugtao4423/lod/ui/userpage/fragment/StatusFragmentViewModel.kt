package sugtao4423.lod.ui.userpage.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ui.BaseTweetListViewModel
import sugtao4423.lod.utils.showToast
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.User

class StatusFragmentViewModel(application: Application) : BaseTweetListViewModel(application) {

    var user: User? = null
    var fragmentType: String = StatusFragment.TYPE_TWEET

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        if (user == null) return@launch

        val paging = Paging(1, 50).let {
            if (maxId > 0) it.maxId(maxId) else it
        }
        val result = withContext(Dispatchers.IO) {
            runCatching { getStatuses(paging) }.getOrNull()
        }
        if (result == null) {
            app.showToast(getErrorToastStringRes())
            return@launch
        }

        if (result.isNotEmpty()) {
            maxId = result.last().id - 1
        }
        hasNextPage = result.isNotEmpty()
        result.let { addStatuses.value = it }
    }

    private fun getStatuses(paging: Paging): ResponseList<Status> = when (fragmentType) {
        StatusFragment.TYPE_TWEET -> app.twitter.getUserTimeline(user!!.id, paging)
        StatusFragment.TYPE_FAVORITE -> app.twitter.getFavorites(user!!.id, paging)
        else -> throw UnsupportedOperationException()
    }

    private fun getErrorToastStringRes(): Int = when (fragmentType) {
        StatusFragment.TYPE_TWEET -> R.string.error_get_timeline
        StatusFragment.TYPE_FAVORITE -> R.string.error_get_favorite
        else -> throw UnsupportedOperationException()
    }

}
