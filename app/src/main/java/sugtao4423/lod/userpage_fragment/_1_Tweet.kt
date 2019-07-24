package sugtao4423.lod.userpage_fragment

import android.os.AsyncTask
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException

class _1_Tweet : UserPageListBaseFragment(FragmentType.TYPE_TWEET) {

    override fun loadList() {
        object : AsyncTask<Unit, Unit, ResponseList<Status>?>() {

            override fun doInBackground(vararg params: Unit?): ResponseList<twitter4j.Status>? {
                return try {
                    if (tweetListAdapter.itemCount > 0) {
                        val tweetId = tweetListAdapter.getItem(tweetListAdapter.itemCount - 1).id
                        app.getTwitter().getUserTimeline(targetUser!!.screenName, Paging(1, 50).maxId(tweetId - 1))
                    } else {
                        app.getTwitter().getUserTimeline(targetUser!!.screenName, Paging(1, 50))
                    }
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: ResponseList<twitter4j.Status>?) {
                if (result != null) {
                    tweetListAdapter.addAll(result)
                    if (targetUser != null && targetUser!!.statusesCount <= tweetListAdapter.itemCount) {
                        isAllLoaded = true
                    }
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_timeline)
                }
                pullToRefresh.isRefreshing = false
                pullToRefresh.isEnabled = true
            }
        }.execute()
    }

}