package sugtao4423.lod

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.runBlocking
import sugtao4423.icondialog.IconDialog
import sugtao4423.icondialog.IconItem
import sugtao4423.lod.databinding.ActivityMainBinding
import sugtao4423.lod.main_fragment.Fragment_home
import sugtao4423.lod.main_fragment.Fragment_mention
import sugtao4423.lod.main_fragment.MainFragmentPagerAdapter
import sugtao4423.lod.ui.addaccount.AddAccountActivity
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
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!app.hasAccount) {
            startActivity(Intent(this, AddAccountActivity::class.java))
            finish()
            return
        }

        binding.tweetBtn.setOnClickListener { clickNewTweet() }
        binding.optionBtn.setOnClickListener { clickOption() }

        val pagerAdapter = MainFragmentPagerAdapter(supportFragmentManager, this)

        binding.pager.apply {
            adapter = pagerAdapter
            currentItem = 1
            offscreenPageLimit = app.account.selectListIds.size + 1
        }

        binding.mainPagerTabStrip.apply {
            tabIndicatorColor = ContextCompat.getColor(applicationContext, R.color.pagerTabText)
            drawFullUnderline = true
        }
        supportActionBar?.setDisplayShowHomeEnabled(false)

        fragmentHome = pagerAdapter.fragmentHome
        fragmentMention = pagerAdapter.fragmentMention
        autoLoadTL()
    }

    private fun autoLoadTL() {
        if (app.account.autoLoadTLInterval == 0) {
            return
        }
        val listener = object : AutoLoadTLService.AutoLoadTLListener {
            override fun onStatus(statuses: ResponseList<Status>) {
                statuses.map {
                    fragmentHome.insert(it)
                    if (app.mentionPattern.matcher(it.text).find() && !it.isRetweet) {
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

    private fun clickNewTweet() {
        startActivity(Intent(this, TweetActivity::class.java))
    }

    private fun clickOption() {
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
        runBlocking { app.reloadAccount() }
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
