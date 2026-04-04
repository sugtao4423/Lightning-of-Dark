package sugtao4423.lod.ui.userpage.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import sugtao4423.lod.databinding.SwipeTweetListBinding
import sugtao4423.lod.ui.adapter.user.UserListAdapter
import sugtao4423.lod.ui.setup
import sugtao4423.lod.ui.userpage.UserPageActivityViewModel

class UserFragment : Fragment() {

    companion object {
        const val KEY_FRAGMENT_TYPE = "fragmentType"

        const val TYPE_FOLLOW = "follow"
        const val TYPE_FOLLOWER = "follower"
    }

    private lateinit var binding: SwipeTweetListBinding

    private val userPageViewModel: UserPageActivityViewModel by activityViewModels()
    private val viewModel: UserFragmentViewModel by viewModels()

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
        viewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = UserListAdapter(requireContext())
        binding.listLine.adapter = adapter

        val scrollListener = viewModel.getLoadMoreListener(binding.listLine.linearLayoutManager)
        binding.listLine.addOnScrollListener(scrollListener)

        userPageViewModel.user.observe(viewLifecycleOwner) {
            viewModel.user = it
        }

        viewModel.addUsers.observe(viewLifecycleOwner) {
            adapter.addAll(it)
        }
        viewModel.onResetList.observe(viewLifecycleOwner) {
            adapter.clear()
            scrollListener.resetState()
        }
    }

}
