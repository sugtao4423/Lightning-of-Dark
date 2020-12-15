package sugtao4423.lod.tweetlistview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class EndlessScrollListener(private val linearLayoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

    var visibleThreshold = 5
    var visibleItemCount = -1
    var totalItemCount = -1
    private var previousTotal = 0
    private var loading = true
    private var currentPage = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        visibleItemCount = recyclerView.childCount
        totalItemCount = linearLayoutManager.itemCount
        val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

        if (loading && totalItemCount > previousTotal) {
            loading = false
            previousTotal = totalItemCount
        }

        if (!loading && (lastVisibleItem + visibleThreshold) > totalItemCount) {
            currentPage++
            onLoadMore(currentPage)
            loading = true
        }
    }

    fun resetState() {
        this.currentPage = 0
        this.previousTotal = 0
        this.loading = true
    }

    abstract fun onLoadMore(currentPage: Int)

}