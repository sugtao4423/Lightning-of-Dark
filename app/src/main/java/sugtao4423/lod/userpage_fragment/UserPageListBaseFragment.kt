package sugtao4423.lod.userpage_fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListUserAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import twitter4j.User

abstract class UserPageListBaseFragment(private val fragmentType: FragmentType) : Fragment() {

    enum class FragmentType {
        TYPE_TWEET,
        TYPE_USER
    }

    protected lateinit var pullToRefresh: SwipeRefreshLayout
    protected lateinit var tweetListAdapter: TweetListAdapter
    protected lateinit var tweetListUserAdapter: TweetListUserAdapter
    protected var cursor = -1L
    protected var isAllLoaded = false
    protected lateinit var app: App
    var targetUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.user_1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = activity!!.applicationContext as App

        val list = view.findViewById<TweetListView>(R.id.userPageList)

        if (fragmentType == FragmentType.TYPE_TWEET) {
            tweetListAdapter = TweetListAdapter(activity!!)
            list.adapter = tweetListAdapter
        } else {
            tweetListUserAdapter = TweetListUserAdapter(activity!!)
            list.adapter = tweetListUserAdapter
        }

        val scrollListener = object : EndlessScrollListener(list.linearLayoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (!isAllLoaded) {
                    loadList()
                }
            }
        }
        list.addOnScrollListener(scrollListener)

        pullToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.userPagePull).apply {
            setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                    android.R.color.holo_orange_light, android.R.color.holo_red_light)
            setOnRefreshListener {
                if (fragmentType == FragmentType.TYPE_TWEET) {
                    tweetListAdapter.clear()
                } else {
                    tweetListUserAdapter.clear()
                }
                cursor = -1L
                isAllLoaded = false
                loadList()
                scrollListener.resetState()
            }
        }
    }

    abstract fun loadList()

}