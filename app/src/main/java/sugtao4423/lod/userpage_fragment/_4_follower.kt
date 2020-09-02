package sugtao4423.lod.userpage_fragment

import android.os.AsyncTask
import kotlinx.android.synthetic.main.user_1.*
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.PagableResponseList
import twitter4j.TwitterException
import twitter4j.User

class _4_follower : UserPageListBaseFragment(FragmentType.TYPE_USER) {

    override fun loadList() {
        object : AsyncTask<Unit, Unit, PagableResponseList<User>?>() {

            override fun doInBackground(vararg params: Unit?): PagableResponseList<User>? {
                return try {
                    app.getTwitter().getFollowersList(targetUser!!.screenName, cursor)
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: PagableResponseList<User>?) {
                if (result != null) {
                    tweetListUserAdapter.addAll(result)
                    cursor = result.nextCursor
                    if (targetUser != null && targetUser!!.followersCount <= tweetListUserAdapter.itemCount) {
                        isAllLoaded = true
                    }
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_follower)
                }
                userPagePull.isRefreshing = false
                userPagePull.isEnabled = true
            }
        }.execute()
    }

}