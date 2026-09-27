package sugtao4423.lod.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.databinding.SwipeTweetListBinding
import sugtao4423.lod.ui.BaseTweetListEvent
import sugtao4423.lod.ui.EndlessScrollListener
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.main.MainActivityViewModel
import sugtao4423.lod.ui.setup

class HomeFragment : Fragment() {

    private lateinit var binding: SwipeTweetListBinding

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: HomeFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SwipeTweetListBinding.inflate(inflater, container, false)
        binding.swipeRefresh.setup {
            viewModel.pull2Refresh()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TweetListAdapter(requireContext())
        binding.listLine.adapter = adapter

        val scrollListener = viewModel.getLoadMoreListener(binding.listLine.linearLayoutManager)
        binding.listLine.addOnScrollListener(scrollListener)

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                mainViewModel.onNewStatuses.collect {
                    adapter.insertTop(it)
                    if (binding.listLine.linearLayoutManager.findFirstVisibleItemPosition() <= 1) {
                        binding.listLine.smoothScrollToPosition(0)
                    }
                }
            }

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.isRefreshing.collect(::updateRefreshState) }
                launch { viewModel.events.collect { handleEvent(it, adapter, scrollListener) } }
            }
        }
        viewModel.loadList(true)
    }

    private fun updateRefreshState(isRefreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = isRefreshing
    }

    private fun handleEvent(
        event: BaseTweetListEvent,
        adapter: TweetListAdapter,
        scrollListener: EndlessScrollListener,
    ) = when (event) {
        is BaseTweetListEvent.ResetList -> {
            adapter.clear()
            scrollListener.resetState()
        }

        is BaseTweetListEvent.AddStatuses -> adapter.addAll(event.statuses)
    }

}
