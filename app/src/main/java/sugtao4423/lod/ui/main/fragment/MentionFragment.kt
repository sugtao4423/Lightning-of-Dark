package sugtao4423.lod.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import sugtao4423.lod.databinding.SwipeTweetListBinding
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.main.MainActivityViewModel

class MentionFragment : Fragment() {

    private lateinit var binding: SwipeTweetListBinding

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: MentionFragmentViewModel by viewModels()

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
        val adapter = TweetListAdapter(requireContext())
        binding.listLine.adapter = adapter

        val scrollListener = viewModel.getLoadMoreListener(binding.listLine.linearLayoutManager)
        binding.listLine.addOnScrollListener(scrollListener)

        mainViewModel.onNewMention.observeForever {
            if (it.isEmpty()) return@observeForever
            adapter.insertTop(it)
            if (binding.listLine.linearLayoutManager.findFirstVisibleItemPosition() <= 1) {
                binding.listLine.smoothScrollToPosition(0)
            }
        }

        viewModel.addStatuses.observe(viewLifecycleOwner) {
            adapter.addAll(it)
        }
        viewModel.onResetList.observe(viewLifecycleOwner) {
            adapter.clear()
            scrollListener.resetState()
        }
        viewModel.loadList()
    }

}
