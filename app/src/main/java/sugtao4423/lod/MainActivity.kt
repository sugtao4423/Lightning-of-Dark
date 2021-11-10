package sugtao4423.lod

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import sugtao4423.icondialog.IconDialog
import sugtao4423.icondialog.IconItem
import sugtao4423.lod.main_fragment.Fragment_home
import sugtao4423.lod.main_fragment.Fragment_mention
import sugtao4423.lod.main_fragment.MainFragmentPagerAdapter
import twitter4j.ResponseList
import twitter4j.Status

class MainActivity : LoDBaseActivity() {

    private var resetFlag = false

    private lateinit var fragmentMention: Fragment_mention
    private lateinit var fragmentHome: Fragment_home

    private var iconDialog: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val pagerAdapter = MainFragmentPagerAdapter(supportFragmentManager, this)

        pager.apply {
            adapter = pagerAdapter
            currentItem = 1
            offscreenPageLimit = app.getCurrentAccount().selectListIds.size + 1
        }

        mainPagerTabStrip.apply {
            tabIndicatorColor = ContextCompat.getColor(applicationContext, R.color.pagerTabText)
            drawFullUnderline = true
        }
        supportActionBar?.setDisplayShowHomeEnabled(false)

        fragmentHome = pagerAdapter.fragmentHome
        fragmentMention = pagerAdapter.fragmentMention
        logIn()
    }

    private fun logIn() {
        if (!app.haveAccount()) {
            startActivity(Intent(this, StartOAuth::class.java))
            finish()
        } else {
            autoLoadTL()
        }
    }

    private fun autoLoadTL() {
        if (app.getCurrentAccount().autoLoadTLInterval == 0) {
            return
        }
        val listener = object : AutoLoadTLService.AutoLoadTLListener {
            override fun onStatus(statuses: ResponseList<Status>) {
                statuses.map {
                    fragmentHome.insert(it)
                    if (app.getMentionPattern().matcher(it.text).find() && !it.isRetweet) {
                        fragmentMention.insert(it)
                    }
                }
            }
        }
        app.autoLoadTLListener = listener
        val intent = Intent(this, AutoLoadTLService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun clickNewTweet(@Suppress("UNUSED_PARAMETER") v: View) {
        startActivity(Intent(this, TweetActivity::class.java))
    }

    fun clickOption(@Suppress("UNUSED_PARAMETER") v: View) {
        if (iconDialog == null) {
            val black = ContextCompat.getColor(applicationContext, R.color.icon)
            val items = arrayOf(
                    IconItem(getString(R.string.icon_bomb)[0], black, getString(R.string.tweet_bomb)),
                    IconItem(getString(R.string.icon_search)[0], black, getString(R.string.search_user)),
                    IconItem(getString(R.string.icon_user)[0], black, getString(R.string.account)),
                    IconItem(getString(R.string.icon_experience)[0], black, getString(R.string.level_info)),
                    IconItem(getString(R.string.icon_clock)[0], black, getString(R.string.use_info)),
                    IconItem(getString(R.string.icon_cog)[0], black, getString(R.string.settings))
            )
            iconDialog = IconDialog(this).setItems(items, OptionClickListener(this))
        }
        iconDialog!!.show()
    }

    fun restart() {
        resetFlag = true
        finish()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, AutoLoadTLService::class.java))
        app.resetAccount()
        app.closeAccountDB()
        app.closeUseTimeDB()
        if (resetFlag) {
            resetFlag = false
            startActivity(Intent(this, MainActivity::class.java))
        }
        clearImageMemoryCache()
    }

    private fun clearImageMemoryCache() {
        Glide.get(this).clearMemory()
    }

}
