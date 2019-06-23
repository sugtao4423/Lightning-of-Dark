package sugtao4423.lod.main_fragment

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sugtao4423.lod.App
import sugtao4423.lod.ListViewListener
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException

class Fragment_home : Fragment() {

    private lateinit var list: TweetListView
    private lateinit var adapter: TweetListAdapter
    private val handler = Handler()
    private lateinit var pullToRefresh: SwipeRefreshLayout
    private var listAsTL = -1L
    private lateinit var app: App

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        app = activity!!.applicationContext as App
        val v = inflater.inflate(R.layout.fragment_list, container, false)
        list = v.findViewById(R.id.listLine)
        listAsTL = app.getCurrentAccount().listAsTL

        adapter = TweetListAdapter(activity!!).apply {
            onItemClickListener = ListViewListener()
            onItemLongClickListener = ListViewListener()
            list.adapter = this
        }
        val scrollListener = getLoadMoreListener()
        list.addOnScrollListener(scrollListener)

        pullToRefresh = v.findViewById(R.id.ListPull)
        pullToRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)
        val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
            adapter.clear()
            loadTimeLine()
            scrollListener.resetState()
        }
        pullToRefresh.setOnRefreshListener(onRefreshListener)
        onRefreshListener.onRefresh()
        return v
    }

    private fun loadTimeLine() {
        object : AsyncTask<Unit, Unit, ResponseList<Status>?>() {

            override fun doInBackground(vararg params: Unit?): ResponseList<twitter4j.Status>? {
                val paging = if (adapter.itemCount > 0) {
                    val tweetId = adapter.getItem(adapter.itemCount - 1).id
                    Paging(1, 50).maxId(tweetId - 1)
                } else {
                    Paging(1, 50)
                }

                return try {
                    if (listAsTL > 0) {
                        app.getTwitter().getUserListStatuses(listAsTL, paging)
                    } else {
                        app.getTwitter().getHomeTimeline(paging)
                    }
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: ResponseList<twitter4j.Status>?) {
                if (result != null) {
                    if (adapter.itemCount <= 0) {
                        app.latestTweetId = result[0].id
                    }
                    adapter.addAll(result)
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_timeline)
                }
                pullToRefresh.isRefreshing = false
                pullToRefresh.isEnabled = true
            }
        }.execute()
    }

    fun insert(status: Status) {
        adapter.insertTop(status)
        app.latestTweetId = status.id
        handler.post {
            if (list.linearLayoutManager.findFirstVisibleItemPosition() <= 1) {
                list.smoothScrollToPosition(0)
            }
        }
    }

    private fun getLoadMoreListener(): EndlessScrollListener {
        return object : EndlessScrollListener(list.linearLayoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (adapter.itemCount > 30) {
                    loadTimeLine()
                }
            }
        }
    }

}