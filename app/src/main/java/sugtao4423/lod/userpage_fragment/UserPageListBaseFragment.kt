package sugtao4423.lod.userpage_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import sugtao4423.lod.App
import sugtao4423.lod.databinding.User1Binding
import sugtao4423.lod.tweetlistview.EndlessScrollListener
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListUserAdapter
import twitter4j.User

abstract class UserPageListBaseFragment(private val fragmentType: FragmentType) : Fragment() {

    enum class FragmentType {
        TYPE_TWEET,
        TYPE_USER
    }

    protected lateinit var binding: User1Binding
    protected lateinit var tweetListAdapter: TweetListAdapter
    protected lateinit var tweetListUserAdapter: TweetListUserAdapter
    protected var cursor = -1L
    protected var isAllLoaded = false
    protected lateinit var app: App
    var targetUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = User1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as App

        if (fragmentType == FragmentType.TYPE_TWEET) {
            tweetListAdapter = TweetListAdapter(requireContext())
            binding.userPageList.adapter = tweetListAdapter
        } else {
            tweetListUserAdapter = TweetListUserAdapter(requireContext())
            binding.userPageList.adapter = tweetListUserAdapter
        }

        val scrollListener = object : EndlessScrollListener(binding.userPageList.linearLayoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (!isAllLoaded) {
                    loadList()
                }
            }
        }
        binding.userPageList.addOnScrollListener(scrollListener)

        binding.userPagePull.apply {
            setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                    android.R.color.holo_orange_light, android.R.color.holo_red_light)
            setOnRefreshListener {
                if (fragmentType == FragmentType.TYPE_TWEET) {
                    tweetListAdapter.clear()
                } else {
                    tweetListUserAdapter.clear()
                }
                cursor = -1L
                isAllLoaded = false
                loadList()
                scrollListener.resetState()
            }
        }
    }

    abstract fun loadList()

}