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
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.tweetlistview.TweetListUserAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import twitter4j.PagableResponseList
import twitter4j.TwitterException
import twitter4j.User

class _4_follower : Fragment() {

    private lateinit var pullToRefresh: SwipeRefreshLayout
    private lateinit var adapter: TweetListUserAdapter
    private var cursor = -1L
    private var isAllLoaded = false
    private lateinit var app: App
    var targetUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        val v = inflater.inflate(R.layout.user_1, container, false)
        app = activity!!.applicationContext as App

        val userFollower = v.findViewById<TweetListView>(R.id.userPageList)

        adapter = TweetListUserAdapter(activity!!)
        userFollower.adapter = adapter

        val scrollListener = getLoadMoreListener(userFollower.linearLayoutManager)
        userFollower.addOnScrollListener(scrollListener)

        pullToRefresh = v.findViewById<SwipeRefreshLayout>(R.id.userPagePull).apply {
            setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                    android.R.color.holo_orange_light, android.R.color.holo_red_light)
            setOnRefreshListener {
                adapter.clear()
                cursor = -1L
                isAllLoaded = false
                loadFollowerLine()
                scrollListener.resetState()
            }
        }
        return v
    }

    private fun getLoadMoreListener(llm: LinearLayoutManager): EndlessScrollListener {
        return object : EndlessScrollListener(llm) {
            override fun onLoadMore(currentPage: Int) {
                if (!isAllLoaded) {
                    loadFollowerLine()
                }
            }
        }
    }

    private fun loadFollowerLine() {
        object : AsyncTask<Unit, Unit, PagableResponseList<User>?>() {

            override fun doInBackground(vararg params: Unit?): PagableResponseList<User>? {
                return try {
                    app.getTwitter().getFollowersList(targetUser!!.screenName, cursor)
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: PagableResponseList<User>?) {
                if (result != null) {
                    adapter.addAll(result)
                    cursor = result.nextCursor
                    if (targetUser != null && targetUser!!.followersCount <= adapter.itemCount) {
                        isAllLoaded = true
                    }
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_follower)
                }
                pullToRefresh.isRefreshing = false
                pullToRefresh.isEnabled = true
            }
        }.execute()
    }

}