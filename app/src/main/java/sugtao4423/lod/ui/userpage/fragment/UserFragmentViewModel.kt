package sugtao4423.lod.ui.userpage.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ui.BaseTweetListViewModel
import sugtao4423.lod.utils.showToast
import twitter4j.PagableResponseList
import twitter4j.User

class UserFragmentViewModel(application: Application) : BaseTweetListViewModel(application) {

    var user: User? = null
    var fragmentType: String = UserFragment.TYPE_FOLLOW

    val addUsers = LiveEvent<PagableResponseList<User>>()
    private var cursor = -1L

    override fun pull2Refresh() {
        cursor = -1L
        super.pull2Refresh()
    }

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        if (user == null) return@launch

        val result = withContext(Dispatchers.IO) {
            runCatching { getUsers() }.getOrNull()
        }
        if (result == null) {
            app.showToast(getErrorToastStringRes())
            return@launch
        }

        cursor = result.nextCursor
        hasNextPage = result.hasNext()
        result.let { addUsers.value = it }
    }

    private fun getUsers() = when (fragmentType) {
        UserFragment.TYPE_FOLLOW -> app.twitter.getFriendsList(user!!.id, cursor, 200)
        UserFragment.TYPE_FOLLOWER -> app.twitter.getFollowersList(user!!.id, cursor, 200)
        else -> throw  UnsupportedOperationException()
    }

    private fun getErrorToastStringRes(): Int = when (fragmentType) {
        UserFragment.TYPE_FOLLOW -> R.string.error_get_follow
        UserFragment.TYPE_FOLLOWER -> R.string.error_get_follower
        else -> throw UnsupportedOperationException()
    }

}
