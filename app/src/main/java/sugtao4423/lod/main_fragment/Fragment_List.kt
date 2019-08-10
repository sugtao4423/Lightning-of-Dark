package sugtao4423.lod.main_fragment

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
import sugtao4423.lod.TwitterList
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.tweetlistview.TweetListView
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException

class Fragment_List : Fragment() {

    companion object {
        const val LIST_INDEX = "listIndex"
    }

    private lateinit var app: App
    private lateinit var thisList: TwitterList
    private lateinit var pullToRefresh: SwipeRefreshLayout
    private var listIndex = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        listIndex = arguments?.getInt(LIST_INDEX)
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater.inflate(R.layout.fragment_list, container, false)
        app = activity!!.applicationContext as App
        thisList = app.getLists(activity!!)[listIndex]
        val list = v.findViewById<TweetListView>(R.id.listLine)
        val adapter = thisList.adapter
        list.adapter = adapter

        val scrollListener = getLoadMoreListener(list.linearLayoutManager)
        list.addOnScrollListener(scrollListener)

        pullToRefresh = v.findViewById<SwipeRefreshLayout>(R.id.listPull).apply {
            setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                    android.R.color.holo_orange_light, android.R.color.holo_red_light)
            setOnRefreshListener {
                adapter.clear()
                thisList.isAlreadyLoad = false
                getList()
                scrollListener.resetState()
            }
        }
        if (thisList.isAppStartLoad) {
            getList()
        }
        return v
    }

    private fun getLoadMoreListener(llm: LinearLayoutManager): EndlessScrollListener {
        return object : EndlessScrollListener(llm) {
            override fun onLoadMore(currentPage: Int) {
                if (thisList.adapter.itemCount > 30) {
                    getList()
                }
            }
        }
    }

    private fun getList() {
        object : AsyncTask<Unit, Unit, ResponseList<Status>?>() {

            override fun doInBackground(vararg params: Unit?): ResponseList<twitter4j.Status>? {
                return try {
                    if (thisList.isAlreadyLoad) {
                        val lastTweetId = thisList.adapter.data.last().id
                        app.getTwitter().getUserListStatuses(thisList.listId, Paging(1, 50).maxId(lastTweetId - 1))
                    } else {
                        app.getTwitter().getUserListStatuses(thisList.listId, Paging(1, 50))
                    }
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: ResponseList<twitter4j.Status>?) {
                if (result != null) {
                    thisList.adapter.addAll(result)
                    thisList.isAlreadyLoad = true
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_list)
                }
                pullToRefresh.isRefreshing = false
                pullToRefresh.isEnabled = true
            }
        }.execute()
    }

}