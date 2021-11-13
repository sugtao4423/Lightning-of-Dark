package sugtao4423.lod.userpage_fragment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.Paging
import twitter4j.TwitterException

class _2_favorites : UserPageListBaseFragment(FragmentType.TYPE_TWEET) {

    override fun loadList() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    if (tweetListAdapter.itemCount > 0) {
                        val tweetId = tweetListAdapter.data.last().id
                        app.getTwitter().getFavorites(targetUser!!.screenName, Paging(1, 50).maxId(tweetId - 1))
                    } else {
                        app.getTwitter().getFavorites(targetUser!!.screenName, Paging(1, 50))
                    }
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                tweetListAdapter.addAll(result)
                if (targetUser != null && targetUser!!.favouritesCount <= tweetListAdapter.itemCount) {
                    isAllLoaded = true
                }
            } else {
                ShowToast(requireContext().applicationContext, R.string.error_get_favorite)
            }
            binding.userPagePull.isRefreshing = false
            binding.userPagePull.isEnabled = true
        }
    }

}