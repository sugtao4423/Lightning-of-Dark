package sugtao4423.lod.ui.userpage

import android.os.Bundle
import androidx.activity.viewModels
import sugtao4423.lod.LoDBaseActivity
import sugtao4423.lod.databinding.ActivityUserPageBinding
import twitter4j.User

class UserPageActivity : LoDBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_USER_OBJECT = "userObject"
        const val INTENT_EXTRA_KEY_USER_SCREEN_NAME = "userScreenName"
    }

    private val viewModel: UserPageActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        val binding = ActivityUserPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = UserPageFragmentPagerAdapter(supportFragmentManager, this)
        binding.viewPager.let {
            it.adapter = adapter
            it.offscreenPageLimit = 5
        }

        viewModel.actionBarTitle.observe(this) {
            supportActionBar?.title = it
        }
        viewModel.onFinish.observe(this) {
            finish()
        }

        intent.getSerializableExtra(INTENT_EXTRA_KEY_USER_OBJECT)?.let {
            viewModel.setUser(it as User)
        }
        intent.getStringExtra(INTENT_EXTRA_KEY_USER_SCREEN_NAME)?.let {
            viewModel.setUser(it)
        }
    }

}
