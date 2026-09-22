package sugtao4423.lod.ui.userpage

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.databinding.ActivityUserPageBinding
import sugtao4423.lod.ui.LoDBaseActivity
import sugtao4423.twitter4j.User

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
        binding.pagerTabStrip.drawFullUnderline = true

        val adapter = UserPageFragmentPagerAdapter(supportFragmentManager, this)
        binding.viewPager.let {
            it.adapter = adapter
            it.offscreenPageLimit = 5
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }

        intent.getSerializableExtra(INTENT_EXTRA_KEY_USER_OBJECT)?.let {
            viewModel.setUser(it as User)
        }
        intent.getStringExtra(INTENT_EXTRA_KEY_USER_SCREEN_NAME)?.let {
            viewModel.setUser(it)
        }
    }

    private fun render(state: UserPageUiState) {
        supportActionBar?.title = state.actionBarTitle
    }

    private fun handleEvent(event: UserPageEvent) = when (event) {
        UserPageEvent.Finish -> finish()
    }

}
