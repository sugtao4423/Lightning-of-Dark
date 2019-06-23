package sugtao4423.lod.userpage_fragment

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
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
import twitter4j.*

class _1_Tweet : Fragment() {

    private lateinit var pullToRefresh: SwipeRefreshLayout
    private lateinit var adapter: TweetListAdapter
    private var isAllLoaded = false
    private lateinit var app: App
    var targetUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        val v = inflater.inflate(R.layout.user_1, container, false)
        app = activity!!.applicationContext as App

        val userTweet = v.findViewById<TweetListView>(R.id.UserPageList)

        adapter = TweetListAdapter(activity!!).apply {
            onItemClickListener = ListViewListener()
            onItemLongClickListener = ListViewListener()
            userTweet.adapter = this
        }

        val scrollListener = getLoadMoreListener(userTweet.linearLayoutManager)
        userTweet.addOnScrollListener(scrollListener)

        pullToRefresh = v.findViewById<SwipeRefreshLayout>(R.id.UserPagePull).apply {
            setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                    android.R.color.holo_orange_light, android.R.color.holo_red_light)
            setOnRefreshListener {
                adapter.clear()
                isAllLoaded = false
                loadTimeLine()
                scrollListener.resetState()
            }
        }
        return v
    }

    private fun getLoadMoreListener(llm: LinearLayoutManager): EndlessScrollListener {
        return object : EndlessScrollListener(llm) {
            override fun onLoadMore(currentPage: Int) {
                if (!isAllLoaded) {
                    loadTimeLine()
                }
            }
        }
    }

    private fun loadTimeLine() {
        object : AsyncTask<Unit, Unit, ResponseList<Status>?>() {

            override fun doInBackground(vararg params: Unit?): ResponseList<twitter4j.Status>? {
                return try {
                    if (adapter.itemCount > 0) {
                        val tweetId = adapter.getItem(adapter.itemCount - 1).id
                        app.getTwitter().getUserTimeline(targetUser!!.screenName, Paging(1, 50).maxId(tweetId - 1))
                    } else {
                        app.getTwitter().getUserTimeline(targetUser!!.screenName, Paging(1, 50))
                    }
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: ResponseList<twitter4j.Status>?) {
                if (result != null) {
                    adapter.addAll(result)
                    if (targetUser != null && targetUser!!.statusesCount <= adapter.itemCount) {
                        isAllLoaded = true
                    }
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_timeline)
                }
                pullToRefresh.isRefreshing = false
                pullToRefresh.isEnabled = true
            }
        }.execute()
    }

}