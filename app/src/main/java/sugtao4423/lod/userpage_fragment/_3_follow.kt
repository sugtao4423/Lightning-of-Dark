package sugtao4423.lod.userpage_fragment

import android.os.AsyncTask
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.PagableResponseList
import twitter4j.TwitterException
import twitter4j.User

class _3_follow : UserPageListBaseFragment(FragmentType.TYPE_USER) {

    override fun loadList() {
        object : AsyncTask<Unit, Unit, PagableResponseList<User>?>() {

            override fun doInBackground(vararg params: Unit?): PagableResponseList<User>? {
                return try {
                    app.getTwitter().getFriendsList(targetUser!!.screenName, cursor)
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: PagableResponseList<User>?) {
                if (result != null) {
                    tweetListUserAdapter.addAll(result)
                    cursor = result.nextCursor
                    if (targetUser != null && targetUser!!.friendsCount <= tweetListUserAdapter.itemCount) {
                        isAllLoaded = true
                    }
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_follow)
                }
                pullToRefresh.isRefreshing = false
                pullToRefresh.isEnabled = true
            }
        }.execute()
    }

}