package sugtao4423.lod.ui.userpage.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.ui.BaseTweetListEvent
import sugtao4423.lod.ui.BaseTweetListFragment
import sugtao4423.lod.ui.adapter.user.UserListAdapter
import sugtao4423.lod.ui.userpage.UserPageActivityViewModel

class UserFragment : BaseTweetListFragment() {

    companion object {
        const val KEY_FRAGMENT_TYPE = "fragmentType"

        const val TYPE_FOLLOW = "follow"
        const val TYPE_FOLLOWER = "follower"
    }

    override val viewModel: UserFragmentViewModel by viewModels()
    private val userPageViewModel: UserPageActivityViewModel by activityViewModels()

    private val userAdapter by lazy { UserListAdapter(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.fragmentType = requireArguments().getString(KEY_FRAGMENT_TYPE)!!
        super.onViewCreated(view, savedInstanceState)

        binding.listLine.adapter = userAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userPageViewModel.uiState.collect { it.user?.let { u -> viewModel.user = u } }
                }
                launch { viewModel.addUsers.collect(userAdapter::addAll) }
            }
        }
    }

    override fun handleEvent(event: BaseTweetListEvent) {
        if (event is BaseTweetListEvent.ResetList) {
            userAdapter.clear()
        }
        super.handleEvent(event)
    }

}
