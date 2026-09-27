package sugtao4423.lod.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import sugtao4423.lod.App
import sugtao4423.twitter4j.Status
import sugtao4423.twitterweb4j.model.CursorList

sealed interface BaseTweetListEvent {
    data object ResetList : BaseTweetListEvent
    data class AddStatuses(val statuses: CursorList<Status>) : BaseTweetListEvent
}

abstract class BaseTweetListViewModel(application: Application) : AndroidViewModel(application) {

    protected val app by lazy { getApplication<App>() }
    protected val tweetCount = App.DEFAULT_TWEET_COUNT

    protected val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    protected val _events = Channel<BaseTweetListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    protected var hasNextPage = true
    protected var bottomCursor: String? = null

    fun getLoadMoreListener(llm: LinearLayoutManager): EndlessScrollListener {
        return object : EndlessScrollListener(llm) {
            override fun onLoadMore(currentPage: Int) {
                if (hasNextPage) loadList(false)
            }
        }
    }

    open fun pull2Refresh() {
        _isRefreshing.update { true }
        _events.trySend(BaseTweetListEvent.ResetList)
        hasNextPage = true
        bottomCursor = null
        loadList(true).invokeOnCompletion {
            _isRefreshing.update { false }
        }
    }

    abstract fun loadList(isRefresh: Boolean = false): Job

}
