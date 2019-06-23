package sugtao4423.lod.userpage_fragment

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerTabStrip
import android.support.v4.view.ViewPager
import sugtao4423.lod.LoDBaseActivity
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.TwitterException
import twitter4j.User

class UserPage : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_USER_OBJECT = "userObject"
        const val INTENT_EXTRA_KEY_USER_SCREEN_NAME = "userScreenName"
    }

    private lateinit var adapter: UserPageFragmentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userpage)

        adapter = UserPageFragmentPagerAdapter(supportFragmentManager, this)
        findViewById<ViewPager>(R.id.Userpager).let {
            it.adapter = adapter
            it.offscreenPageLimit = 5
        }

        findViewById<PagerTabStrip>(R.id.userPagerTabStrip).apply {
            tabIndicatorColor = ContextCompat.getColor(applicationContext, R.color.pagerTabText)
            drawFullUnderline = true
        }
        supportActionBar?.setDisplayShowHomeEnabled(false)

        intent.getSerializableExtra(INTENT_EXTRA_KEY_USER_OBJECT).let {
            if (it != null) {
                setTargetUser(it as User)
                return
            }
        }

        object : AsyncTask<Unit, Unit, User?>() {

            override fun doInBackground(vararg params: Unit?): User? {
                return try {
                    app.getTwitter().showUser(intent.getStringExtra(INTENT_EXTRA_KEY_USER_SCREEN_NAME))
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: User?) {
                if (result != null) {
                    setTargetUser(result)
                    (adapter.getItem(0) as _0_detail).setText()
                } else {
                    ShowToast(applicationContext, R.string.error_get_user_detail)
                    finish()
                }
            }
        }.execute()
    }

    private fun setTargetUser(target: User) {
        supportActionBar?.title = target.name
        (adapter.getItem(0) as _0_detail).targetUser = target
        (adapter.getItem(1) as _1_Tweet).targetUser = target
        (adapter.getItem(2) as _2_favorites).targetUser = target
        (adapter.getItem(3) as _3_follow).targetUser = target
        (adapter.getItem(4) as _4_follower).targetUser = target
    }


}