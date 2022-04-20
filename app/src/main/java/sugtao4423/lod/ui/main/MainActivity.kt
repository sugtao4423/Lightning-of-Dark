package sugtao4423.lod.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.runBlocking
import sugtao4423.icondialog.IconDialog
import sugtao4423.icondialog.IconItem
import sugtao4423.lod.AutoLoadTLService
import sugtao4423.lod.LoDBaseActivity
import sugtao4423.lod.R
import sugtao4423.lod.TweetActivity
import sugtao4423.lod.databinding.ActivityMainBinding
import sugtao4423.lod.ui.addaccount.AddAccountActivity
import sugtao4423.lod.ui.main.listener.OptionClickListener

class MainActivity : LoDBaseActivity() {

    private var resetFlag = false
    private var iconDialog: AlertDialog.Builder? = null

    private val viewModel: MainActivityViewModel by viewModels()
    private val optionViewModel: OptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.apply {
            hide()
            setDisplayShowHomeEnabled(false)
        }
        val binding = ActivityMainBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)

        if (!viewModel.hasAccount) {
            startActivity(Intent(this, AddAccountActivity::class.java))
            finish()
            return
        }

        viewModel.onStartTweetActivity.observe(this) {
            startNewTweetActivity()
        }
        viewModel.showOptionDialog.observe(this) {
            showOptionDialog()
        }
        viewModel.onStartAutoLoadTLService.observe(this) {
            startAutoLoadTLService()
        }

        val listData = viewModel.listData
        binding.viewPager.apply {
            adapter = MainFragmentPagerAdapter(supportFragmentManager, this@MainActivity, listData)
            currentItem = 1
            offscreenPageLimit = listData.size + 1
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
        val black = ContextCompat.getColor(applicationContext, R.color.icon)
        val items = arrayOf(
            IconItem(getString(R.string.icon_bomb)[0], black, getString(R.string.tweet_bomb)),
            IconItem(getString(R.string.icon_search)[0], black, getString(R.string.search_user)),
            IconItem(getString(R.string.icon_user)[0], black, getString(R.string.account)),
            IconItem(getString(R.string.icon_experience)[0], black, getString(R.string.level_info)),
            IconItem(getString(R.string.icon_clock)[0], black, getString(R.string.use_info)),
            IconItem(getString(R.string.icon_cog)[0], black, getString(R.string.settings))
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
