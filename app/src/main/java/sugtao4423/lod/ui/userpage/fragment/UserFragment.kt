package sugtao4423.lod.ui.userpage.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.ui.UserListFragment
import sugtao4423.lod.ui.userpage.UserPageActivityViewModel

class UserFragment : UserListFragment() {

    companion object {
        const val KEY_FRAGMENT_TYPE = "fragmentType"

        const val TYPE_FOLLOW = "follow"
        const val TYPE_FOLLOWER = "follower"
    }

    override val viewModel: UserFragmentViewModel by viewModels()
    private val userPageViewModel: UserPageActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.fragmentType = requireArguments().getString(KEY_FRAGMENT_TYPE)!!
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userPageViewModel.uiState.collect { it.user?.let { u -> viewModel.user = u } }
            }
        }
    }

}
