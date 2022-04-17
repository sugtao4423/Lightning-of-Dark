package sugtao4423.lod.userpage_fragment

import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.LoDBaseActivity
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.databinding.UserpageBinding
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
        val binding = UserpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UserPageFragmentPagerAdapter(supportFragmentManager, this)
        binding.userPager.let {
            it.adapter = adapter
            it.offscreenPageLimit = 5
        }

        binding.userPagerTabStrip.apply {
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

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    app.twitter.showUser(intent.getStringExtra(INTENT_EXTRA_KEY_USER_SCREEN_NAME))
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                setTargetUser(result)
            } else {
                ShowToast(applicationContext, R.string.error_get_user_detail)
                finish()
            }
        }
    }

    private fun setTargetUser(target: User) {
        supportActionBar?.title = target.name
        adapter.setTargetUser(target)
    }

}
