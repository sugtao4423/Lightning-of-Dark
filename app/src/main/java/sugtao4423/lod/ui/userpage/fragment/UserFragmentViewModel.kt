package sugtao4423.lod.ui.userpage.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ui.BaseTweetListViewModel
import sugtao4423.lod.utils.showToast
import sugtao4423.twitterweb4j.model.PagableCursorList
import twitter4j.User

class UserFragmentViewModel(application: Application) : BaseTweetListViewModel(application) {

    private val userCount = App.DEFAULT_USER_COUNT

    var user: User? = null
    var fragmentType: String = UserFragment.TYPE_FOLLOW

    val addUsers = LiveEvent<PagableCursorList<User>>()

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        if (user == null) return@launch

        val result = withContext(Dispatchers.IO) {
            runCatching { getUsers() }.getOrNull()
        }
        if (result == null) {
            app.showToast(getErrorToastStringRes())
            return@launch
        }

        bottomCursor = result.cursorBottom
        hasNextPage = result.hasNext()
        addUsers.value = result
    }

    private fun getUsers() = when (fragmentType) {
        UserFragment.TYPE_FOLLOW -> app.twitter.following(user!!.id, userCount, bottomCursor)
        UserFragment.TYPE_FOLLOWER -> app.twitter.followers(user!!.id, userCount, bottomCursor)
        else -> throw UnsupportedOperationException()
    }

    private fun getErrorToastStringRes(): Int = when (fragmentType) {
        UserFragment.TYPE_FOLLOW -> R.string.error_get_follow
        UserFragment.TYPE_FOLLOWER -> R.string.error_get_follower
        else -> throw UnsupportedOperationException()
    }

}
