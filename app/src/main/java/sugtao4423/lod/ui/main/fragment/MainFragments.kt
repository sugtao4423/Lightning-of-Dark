package sugtao4423.lod.ui.main.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sugtao4423.lod.ui.main.MainActivityViewModel

class MentionFragment : BaseFragment() {

    override val viewModel: MentionFragmentViewModel by viewModels()
    private val mainViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.onNewMention.collect(::insertTop)
        }
        viewModel.loadList()
    }

}

class HomeFragment : BaseFragment() {

    override val viewModel: HomeFragmentViewModel by viewModels()
    private val mainViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.onNewStatuses.collect(::insertTop)
        }
        viewModel.loadList(true)
    }

}

class ListFragment : BaseFragment() {

    companion object {
        const val LIST_INDEX = "listIndex"
    }

    override val viewModel: ListFragmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.listIndex = requireArguments().getInt(LIST_INDEX)
        super.onViewCreated(view, savedInstanceState)
    }

}
