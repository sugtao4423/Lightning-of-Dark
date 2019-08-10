package sugtao4423.lod.main_fragment

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException

class Fragment_mention : Fragment() {

    private lateinit var list: TweetListView
    private lateinit var pullToRefresh: SwipeRefreshLayout
    private lateinit var adapter: TweetListAdapter
    private lateinit var app: App

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        app = activity!!.applicationContext as App
        val v = inflater.inflate(R.layout.fragment_list, container, false)
        list = v.findViewById(R.id.listLine)

        adapter = TweetListAdapter(activity!!)
        list.adapter = adapter

        val scrollListener = getLoadMoreListener()
        list.addOnScrollListener(scrollListener)

        pullToRefresh = v.findViewById(R.id.listPull)
        pullToRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)
        val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
            adapter.clear()
            loadMention()
            scrollListener.resetState()
        }
        pullToRefresh.setOnRefreshListener(onRefreshListener)
        onRefreshListener.onRefresh()
        return v
    }

    private fun getLoadMoreListener(): EndlessScrollListener {
        return object : EndlessScrollListener(list.linearLayoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (adapter.itemCount > 30) {
                    loadMention()
                }
            }
        }
    }

    private fun loadMention() {
        object : AsyncTask<Unit, Unit, ResponseList<Status>?>() {

            override fun doInBackground(vararg params: Unit?): ResponseList<twitter4j.Status>? {
                return try {
                    if (adapter.itemCount > 0) {
                        val tweetId = adapter.data.last().id
                        app.getTwitter().getMentionsTimeline(Paging(1, 50).maxId(tweetId - 1))
                    } else {
                        app.getTwitter().getMentionsTimeline(Paging(1, 50))
                    }
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: ResponseList<twitter4j.Status>?) {
                if (result != null) {
                    addAll(result)
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_mention)
                }
                pullToRefresh.isRefreshing = false
                pullToRefresh.isEnabled = true
            }
        }.execute()
    }

    fun insert(status: Status) {
        adapter.insertTop(status)
        if (list.linearLayoutManager.findFirstVisibleItemPosition() <= 1) {
            list.smoothScrollToPosition(0)
        }
    }

    private fun addAll(statuses: ResponseList<Status>) {
        adapter.addAll(statuses)
    }

}