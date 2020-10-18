package sugtao4423.lod.main_fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.tweetlistview.TweetListAdapter
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException

class Fragment_mention : Fragment() {

    private lateinit var adapter: TweetListAdapter
    private lateinit var app: App

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = activity!!.applicationContext as App

        adapter = TweetListAdapter(activity!!)
        listLine.adapter = adapter

        val scrollListener = getLoadMoreListener()
        listLine.addOnScrollListener(scrollListener)

        listPull2Refresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)
        val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
            adapter.clear()
            loadMention()
            scrollListener.resetState()
        }
        listPull2Refresh.setOnRefreshListener(onRefreshListener)
        onRefreshListener.onRefresh()
    }

    private fun getLoadMoreListener(): EndlessScrollListener {
        return object : EndlessScrollListener(listLine.linearLayoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (adapter.itemCount > 30) {
                    loadMention()
                }
            }
        }
    }

    private fun loadMention() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
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
            if (result != null) {
                addAll(result)
            } else {
                ShowToast(activity!!.applicationContext, R.string.error_get_mention)
            }
            listPull2Refresh.isRefreshing = false
            listPull2Refresh.isEnabled = true
        }
    }

    fun insert(status: Status) {
        adapter.insertTop(status)
        if (listLine.linearLayoutManager.findFirstVisibleItemPosition() <= 1) {
            listLine.smoothScrollToPosition(0)
        }
    }

    private fun addAll(statuses: ResponseList<Status>) {
        adapter.addAll(statuses)
    }

}