package sugtao4423.lod.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import sugtao4423.lod.databinding.SwipeTweetListBinding
import sugtao4423.lod.ui.adapter.ListUpdatable
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.adapter.user.UserListAdapter
import sugtao4423.twitter4j.Status
import sugtao4423.twitter4j.User
import sugtao4423.twitterweb4j.model.CursorList
import sugtao4423.twitterweb4j.model.PagableCursorList

sealed class BaseListFragment<T, L : List<T>, A>(
    adapterFactory: (context: Context) -> A,
) : Fragment() where A : RecyclerView.Adapter<*>, A : ListUpdatable<T> {

    protected abstract val viewModel: BaseListViewModel<T, L>

    protected lateinit var binding: SwipeTweetListBinding

    protected val adapter by lazy { adapterFactory(requireContext()) }
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

    protected open fun insertTop(items: List<T>) {
        adapter.insertTop(items)
        if (binding.listLine.linearLayoutManager.findFirstVisibleItemPosition() <= 1) {
            binding.listLine.smoothScrollToPosition(0)
        }
    }

    protected open fun updateRefreshState(isRefreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = isRefreshing
    }

    protected open fun handleEvent(event: BaseListEvent<T, L>) = when (event) {
        is BaseListEvent.ResetList -> {
            adapter.clear()
            scrollListener.resetState()
        }

        is BaseListEvent.AddItems<T, L> -> adapter.addAll(event.items)
    }

}

abstract class TweetListFragment : BaseListFragment<Status, CursorList<Status>, TweetListAdapter>({
    TweetListAdapter(it)
})

abstract class UserListFragment : BaseListFragment<User, PagableCursorList<User>, UserListAdapter>({
    UserListAdapter(it)
})
