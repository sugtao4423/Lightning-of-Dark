package sugtao4423.lod.main_fragment

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_list.*
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.TwitterList
import sugtao4423.lod.tweetlistview.EndlessScrollListener
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
    private var listIndex = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listIndex = arguments?.getInt(LIST_INDEX)!!
        app = activity!!.applicationContext as App
        thisList = app.getLists(activity!!)[listIndex]
        val adapter = thisList.adapter
        listLine.adapter = adapter

        val scrollListener = getLoadMoreListener(listLine.linearLayoutManager)
        listLine.addOnScrollListener(scrollListener)

        listPull2Refresh.apply {
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
                listPull2Refresh.isRefreshing = false
                listPull2Refresh.isEnabled = true
            }
        }.execute()
    }

}