package sugtao4423.lod.main_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.databinding.FragmentListBinding
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.tweetlistview.TweetListAdapter
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException

class Fragment_mention : Fragment() {

    private lateinit var binding: FragmentListBinding
    private lateinit var adapter: TweetListAdapter
    private lateinit var app: App

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as App

        adapter = TweetListAdapter(requireContext())
        binding.listLine.adapter = adapter

        val scrollListener = getLoadMoreListener()
        binding.listLine.addOnScrollListener(scrollListener)

        binding.listPull2Refresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)
        val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
            adapter.clear()
            loadMention()
            scrollListener.resetState()
        }
        binding.listPull2Refresh.setOnRefreshListener(onRefreshListener)
        onRefreshListener.onRefresh()
    }

    private fun getLoadMoreListener(): EndlessScrollListener {
        return object : EndlessScrollListener(binding.listLine.linearLayoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (adapter.hasNextPage) {
                    loadMention()
                }
            }
        }
    }

    private fun loadMention() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                val paging = Paging(1, 50).let {
                    if (adapter.itemCount > 0) it.maxId(adapter.data.last().id - 1) else it
                }

                try {
                    app.getTwitter().getMentionsTimeline(paging)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                if (result.isEmpty()) {
                    adapter.hasNextPage = false
                }
                addAll(result)
            } else {
                ShowToast(requireContext().applicationContext, R.string.error_get_mention)
            }
            binding.listPull2Refresh.isRefreshing = false
            binding.listPull2Refresh.isEnabled = true
        }
    }

    fun insert(status: Status) {
        adapter.insertTop(status)
        if (binding.listLine.linearLayoutManager.findFirstVisibleItemPosition() <= 1) {
            binding.listLine.smoothScrollToPosition(0)
        }
    }

    private fun addAll(statuses: ResponseList<Status>) {
        adapter.addAll(statuses)
    }

}
