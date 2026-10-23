package sugtao4423.lod.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.databinding.ActivityMainBinding
import sugtao4423.lod.service.AutoLoadTLService
import sugtao4423.lod.ui.LoDBaseActivity
import sugtao4423.lod.ui.addaccount.AddAccountActivity
import sugtao4423.lod.ui.icondialog.IconDialog
import sugtao4423.lod.ui.icondialog.IconItem
import sugtao4423.lod.ui.main.listener.OptionClickListener
import sugtao4423.lod.ui.tweet.TweetActivity

class MainActivity : LoDBaseActivity() {

    private var resetFlag = false
    private var iconDialog: IconDialog? = null

    private val viewModel: MainActivityViewModel by viewModels()
    private val optionViewModel: OptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback { finish() }

        supportActionBar?.apply {
            hide()
            setDisplayShowHomeEnabled(false)
        }
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!viewModel.hasAccount) {
            startActivity(Intent(this, AddAccountActivity::class.java))
            finish()
            return
        }

        binding.apply {
            pagerTabStrip.drawFullUnderline = true
            tweetButton.setOnClickListener { startNewTweetActivity() }
            tweetButton.setOnLongClickListener { showOptionDialog(); true }
            optionButton.setOnClickListener { showOptionDialog() }
            optionButton.setOnLongClickListener { startNewTweetActivity(); true }
        }

        lifecycleScope.launch {
            viewModel.onStartAutoLoadTLService.collect {
                startAutoLoadTLService()
            }
        }

        binding.viewPager.apply {
            val listSettings = viewModel.listSettings
            adapter =
                MainFragmentPagerAdapter(supportFragmentManager, this@MainActivity, listSettings)
            currentItem = 1
            offscreenPageLimit = listSettings.size + 1
        }

        viewModel.viewInitialized()
    }

    private fun startAutoLoadTLService() {
        val intent = Intent(this, AutoLoadTLService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun startNewTweetActivity() {
        startActivity(Intent(this, TweetActivity::class.java))
    }

    private fun createOptionDialog() {
        val items = listOf(
            IconItem(R.string.icon_bomb, title = R.string.tweet_bomb),
            IconItem(R.string.icon_search, title = R.string.search_user),
            IconItem(R.string.icon_user, title = R.string.account),
            IconItem(R.string.icon_experience, title = R.string.level_info),
            IconItem(R.string.icon_clock, title = R.string.use_info),
            IconItem(R.string.icon_cog, title = R.string.settings),
        )
        iconDialog = IconDialog(this).setItems(items, OptionClickListener(this, optionViewModel))
    }

    private fun showOptionDialog() {
        if (iconDialog == null) {
            createOptionDialog()
        }
        iconDialog!!.show()
    }

    fun restart() {
        resetFlag = true
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, AutoLoadTLService::class.java))
        runBlocking { (applicationContext as App).reloadAccount() }
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
