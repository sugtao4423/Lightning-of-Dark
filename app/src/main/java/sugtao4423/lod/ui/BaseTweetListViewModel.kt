package sugtao4423.lod.ui

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Job
import sugtao4423.lod.App
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import twitter4j.ResponseList
import twitter4j.Status

abstract class BaseTweetListViewModel(application: Application) : AndroidViewModel(application) {

    protected val app by lazy { getApplication<App>() }

    val isRefreshing = ObservableField(false)

    val addStatuses = LiveEvent<ResponseList<Status>>()
    val onResetList = LiveEvent<Unit>()

    protected var hasNextPage = true
    protected var maxId = -1L

    fun getLoadMoreListener(llm: LinearLayoutManager): EndlessScrollListener {
        return object : EndlessScrollListener(llm) {
            override fun onLoadMore(currentPage: Int) {
                if (hasNextPage) loadList()
            }
        }
    }

    fun pull2Refresh() {
        isRefreshing.set(true)
        onResetList.value = Unit
        hasNextPage = true
        maxId = -1L
        loadList().invokeOnCompletion {
            isRefreshing.set(false)
        }
    }

    abstract fun loadList(): Job

}
