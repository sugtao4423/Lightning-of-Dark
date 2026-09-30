package sugtao4423.lod.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import sugtao4423.lod.App
import sugtao4423.twitter4j.Status
import sugtao4423.twitter4j.User
import sugtao4423.twitterweb4j.model.CursorList
import sugtao4423.twitterweb4j.model.PagableCursorList

sealed interface BaseListEvent<out T, out L : List<T>> {
    data object ResetList : BaseListEvent<Nothing, Nothing>
    data class AddItems<T, L : List<T>>(val items: L) : BaseListEvent<T, L>
}

sealed class BaseListViewModel<T, L : List<T>>(application: Application) :
    AndroidViewModel(application) {

    protected val app by lazy { getApplication<App>() }
    protected val tweetCount = App.DEFAULT_TWEET_COUNT
    protected val userCount = App.DEFAULT_USER_COUNT

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _events = Channel<BaseListEvent<T, L>>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    protected fun addItems(items: L): ChannelResult<Unit> =
        _events.trySend(BaseListEvent.AddItems(items))

    protected var bottomCursor: String? = null

    val loadMoreListener = object : EndlessScrollListener() {
        override fun onLoadMore(): Job = loadList(false)
    }

    fun setHasNextPage(hasNextPage: Boolean) {
        loadMoreListener.hasMore = hasNextPage
    }

    open fun pull2Refresh() {
        _isRefreshing.update { true }
        _events.trySend(BaseListEvent.ResetList)
        setHasNextPage(true)
        bottomCursor = null
        loadList(true).invokeOnCompletion {
            _isRefreshing.update { false }
        }
    }

    abstract fun loadList(isRefresh: Boolean = false): Job

}

abstract class TweetListViewModel(application: Application) :
    BaseListViewModel<Status, CursorList<Status>>(application)

abstract class UserListViewModel(application: Application) :
    BaseListViewModel<User, PagableCursorList<User>>(application)
