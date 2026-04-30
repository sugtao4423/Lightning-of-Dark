package sugtao4423.lod.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Job
import sugtao4423.lod.App
import sugtao4423.twitterweb4j.model.CursorList
import twitter4j.Status

abstract class BaseTweetListViewModel(application: Application) : AndroidViewModel(application) {

    protected val app by lazy { getApplication<App>() }
    protected val tweetCount = App.DEFAULT_TWEET_COUNT

    val isRefreshing = MutableLiveData(false)

    val addStatuses = LiveEvent<CursorList<Status>>()
    val onResetList = LiveEvent<Unit>()

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
        isRefreshing.value = true
        onResetList.value = Unit
        hasNextPage = true
        bottomCursor = null
        loadList(true).invokeOnCompletion {
            isRefreshing.value = false
        }
    }

    abstract fun loadList(isRefresh: Boolean = false): Job

}
