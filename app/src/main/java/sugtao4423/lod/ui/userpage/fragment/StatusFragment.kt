package sugtao4423.lod.ui.userpage.fragment

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
import sugtao4423.lod.ui.setup
import sugtao4423.lod.ui.userpage.UserPageActivityViewModel

class StatusFragment : Fragment() {

    companion object {
        const val KEY_FRAGMENT_TYPE = "fragmentType"

        const val TYPE_TWEET = "tweet"
        const val TYPE_FAVORITE = "favorite"
    }

    private lateinit var binding: SwipeTweetListBinding

    private val userPageViewModel: UserPageActivityViewModel by activityViewModels()
    private val viewModel: StatusFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.fragmentType = requireArguments().getString(KEY_FRAGMENT_TYPE)!!

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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userPageViewModel.uiState.collect { it.user?.let { u -> viewModel.user = u } }
                }
                launch { viewModel.isRefreshing.collect(::updateRefreshState) }
                launch { viewModel.events.collect { handleEvent(it, adapter, scrollListener) } }
            }
        }
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
