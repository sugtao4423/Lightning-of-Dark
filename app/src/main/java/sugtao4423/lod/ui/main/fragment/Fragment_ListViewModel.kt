package sugtao4423.lod.ui.main.fragment

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.ui.main.MainActivityViewModel
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status

class Fragment_ListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val isRefreshing = ObservableField(false)

    var listData: MainActivityViewModel.ListData? = null
        set(value) {
            if (field != value) {
                field = value
                if (value!!.isAppStartLoad) loadList()
            }
        }

    private val _addStatuses = LiveEvent<ResponseList<Status>>()
    val addStatuses: LiveData<ResponseList<Status>> = _addStatuses

    private val _onResetList = LiveEvent<Unit>()
    val onResetList: LiveData<Unit> = _onResetList

    private var hasNextPage = true
    private var maxId = -1L

    fun getLoadMoreListener(llm: LinearLayoutManager): EndlessScrollListener {
        return object : EndlessScrollListener(llm) {
            override fun onLoadMore(currentPage: Int) {
                if (hasNextPage) {
                    loadList()
                }
            }
        }
    }

    fun pull2Refresh() {
        isRefreshing.set(true)
        _onResetList.value = Unit
        hasNextPage = true
        maxId = -1L
        loadList()
    }

    private fun loadList() = viewModelScope.launch {
        val paging = Paging(1, 50).let {
            if (maxId > 0) it.maxId(maxId) else it
        }
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.getUserListStatuses(listData!!.id, paging) }.getOrNull()
        }
        if (result == null) {
            ShowToast(app, R.string.error_get_list)
        } else {
            if (result.isNotEmpty()) {
                maxId = result.last().id - 1
            }
            hasNextPage = result.isNotEmpty()
            result.let { _addStatuses.value = it }
        }

        isRefreshing.set(false)
    }

}
