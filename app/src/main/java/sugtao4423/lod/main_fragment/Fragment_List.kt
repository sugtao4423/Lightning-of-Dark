package sugtao4423.lod.main_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.TwitterList
import sugtao4423.lod.databinding.FragmentListBinding
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import twitter4j.Paging
import twitter4j.TwitterException

class Fragment_List : Fragment() {

    companion object {
        const val LIST_INDEX = "listIndex"
    }

    private lateinit var binding: FragmentListBinding
    private lateinit var app: App
    private lateinit var thisList: TwitterList
    private var listIndex = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listIndex = arguments?.getInt(LIST_INDEX)!!
        app = requireContext().applicationContext as App
        thisList = app.getLists(requireContext())[listIndex]
        val adapter = thisList.adapter
        binding.listLine.adapter = adapter

        val scrollListener = getLoadMoreListener(binding.listLine.linearLayoutManager)
        binding.listLine.addOnScrollListener(scrollListener)

        binding.listPull2Refresh.apply {
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
                if (thisList.adapter.hasNextPage) {
                    getList()
                }
            }
        }
    }

    private fun getList() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                val paging = Paging(1, 50).let {
                    if (thisList.isAlreadyLoad) it.maxId(thisList.adapter.data.last().id - 1) else it
                }

                try {
                    app.twitter.getUserListStatuses(thisList.listId, paging)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                if (result.isEmpty()) {
                    thisList.adapter.hasNextPage = false
                }
                thisList.adapter.addAll(result)
                thisList.isAlreadyLoad = true
            } else {
                ShowToast(requireContext().applicationContext, R.string.error_get_list)
            }
            binding.listPull2Refresh.isRefreshing = false
            binding.listPull2Refresh.isEnabled = true
        }
    }

}
