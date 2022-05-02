package sugtao4423.lod.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import sugtao4423.lod.databinding.SwipeTweetListBinding
import sugtao4423.lod.ui.adapter.TweetListAdapter
import sugtao4423.lod.ui.main.MainActivityViewModel

class ListFragment : Fragment() {

    companion object {
        const val LIST_INDEX = "listIndex"
    }

    private lateinit var binding: SwipeTweetListBinding

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: ListFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SwipeTweetListBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.listData = mainViewModel.listData[requireArguments().getInt(LIST_INDEX)]

        val adapter = TweetListAdapter(requireContext())
        binding.listLine.adapter = adapter

        val scrollListener = viewModel.getLoadMoreListener(binding.listLine.linearLayoutManager)
        binding.listLine.addOnScrollListener(scrollListener)

        viewModel.addStatuses.observe(viewLifecycleOwner) {
            adapter.addAll(it)
        }
        viewModel.onResetList.observe(viewLifecycleOwner) {
            adapter.clear()
            scrollListener.resetState()
        }
    }
}
