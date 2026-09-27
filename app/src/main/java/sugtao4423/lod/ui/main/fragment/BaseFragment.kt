package sugtao4423.lod.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.databinding.SwipeTweetListBinding
import sugtao4423.lod.ui.BaseTweetListEvent
import sugtao4423.lod.ui.BaseTweetListViewModel
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.setup
import sugtao4423.twitter4j.Status

abstract class BaseFragment : Fragment() {

    protected abstract val viewModel: BaseTweetListViewModel

    protected lateinit var binding: SwipeTweetListBinding

    protected val adapter by lazy { TweetListAdapter(requireContext()) }
    protected val scrollListener by lazy {
        viewModel.getLoadMoreListener(binding.listLine.linearLayoutManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = SwipeTweetListBinding.inflate(inflater, container, false)
        binding.swipeRefresh.setup { viewModel.pull2Refresh() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listLine.adapter = adapter
        binding.listLine.addOnScrollListener(scrollListener)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.isRefreshing.collect(::updateRefreshState) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    protected fun insertTop(statues: List<Status>) {
        adapter.insertTop(statues)
        if (binding.listLine.linearLayoutManager.findFirstVisibleItemPosition() <= 1) {
            binding.listLine.smoothScrollToPosition(0)
        }
    }

    protected fun updateRefreshState(isRefreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = isRefreshing
    }

    protected fun handleEvent(event: BaseTweetListEvent) = when (event) {
        is BaseTweetListEvent.ResetList -> {
            adapter.clear()
            scrollListener.resetState()
        }

        is BaseTweetListEvent.AddStatuses -> adapter.addAll(event.statuses)
    }

}
