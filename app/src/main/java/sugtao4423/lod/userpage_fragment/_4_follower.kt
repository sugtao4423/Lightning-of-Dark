package sugtao4423.lod.userpage_fragment

import kotlinx.android.synthetic.main.user_1.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.TwitterException

class _4_follower : UserPageListBaseFragment(FragmentType.TYPE_USER) {

    override fun loadList() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    app.getTwitter().getFollowersList(targetUser!!.screenName, cursor)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                tweetListUserAdapter.addAll(result)
                cursor = result.nextCursor
                if (targetUser != null && targetUser!!.followersCount <= tweetListUserAdapter.itemCount) {
                    isAllLoaded = true
                }
            } else {
                ShowToast(requireContext().applicationContext, R.string.error_get_follower)
            }
            userPagePull.isRefreshing = false
            userPagePull.isEnabled = true
        }
    }

}