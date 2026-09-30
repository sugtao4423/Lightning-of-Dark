package sugtao4423.lod.ui

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job

abstract class EndlessScrollListener : RecyclerView.OnScrollListener() {

    var visibleThreshold = 5
    var hasMore = true
    private var loading = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!hasMore || loading) return

        val lm = recyclerView.layoutManager as LinearLayoutManager
        val lastVisible = lm.findLastVisibleItemPosition()
        if (lastVisible == RecyclerView.NO_POSITION) return

        if (lastVisible >= lm.itemCount - 1 - visibleThreshold) {
            loading = true
            onLoadMore().invokeOnCompletion { loading = false }
        }
    }

    fun resetState() {
        this.loading = false
    }

    abstract fun onLoadMore(): Job

}
