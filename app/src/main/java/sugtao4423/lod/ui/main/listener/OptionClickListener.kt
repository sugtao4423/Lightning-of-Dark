package sugtao4423.lod.ui.main.listener

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.databinding.OptionDialogSearchUserBinding
import sugtao4423.lod.databinding.OptionDialogTweetBombBinding
import sugtao4423.lod.entity.Account
import sugtao4423.lod.ui.addaccount.AddAccountActivity
import sugtao4423.lod.ui.main.MainActivity
import sugtao4423.lod.ui.main.OptionViewModel
import sugtao4423.lod.ui.settings.SettingsActivity
import sugtao4423.lod.ui.userpage.UserPageActivity

class OptionClickListener(
    private val activity: MainActivity,
    private val viewModel: OptionViewModel
) : DialogInterface.OnClickListener {

    init {
        viewModel.onSearchUserScreenName.observe(activity) {
            val intent = Intent(activity, UserPageActivity::class.java).apply {
                putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_SCREEN_NAME, it)
            }
            activity.startActivity(intent)
        }
        viewModel.onGetAllAccounts.observe(activity) {
            showAccountsDialog(it)
        }
        viewModel.onRestartMainActivity.observe(activity) {
            activity.restart()
        }
        viewModel.onShowLevelInfoDialog.observe(activity) {
            AlertDialog.Builder(activity).setMessage(it).setPositiveButton(R.string.ok, null).show()
        }
        viewModel.onShowUseInfoDialog.observe(activity) {
            AlertDialog.Builder(activity).apply {
                setTitle(R.string.use_info)
                setMessage(it)
                setPositiveButton(R.string.ok, null)
                show()
            }
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            0 -> tweetBomb()
            1 -> searchUser()
            2 -> viewModel.doGetAllAccounts()
            3 -> viewModel.showLevelInfo()
            4 -> viewModel.showUseTimeInfo()
            5 -> activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
    }

    private fun tweetBomb() {
        val staticText = MutableLiveData("")
        val loopText = MutableLiveData("")
        val loopCount = MutableLiveData("")
        val binding = OptionDialogTweetBombBinding.inflate(activity.layoutInflater).also {
            it.staticText = staticText
            it.loopText = loopText
            it.loopCount = loopCount
        }
        AlertDialog.Builder(activity).apply {
            setView(binding.root)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.doBombTweet(staticText.value!!, loopText.value!!, loopCount.value!!)
            }
            show()
        }
    }

    private fun searchUser() {
        val screenName = MutableLiveData("")
        val binding = OptionDialogSearchUserBinding.inflate(activity.layoutInflater).also {
            it.screenName = screenName
        }
        AlertDialog.Builder(activity).apply {
            setView(binding.root)
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.doSearchUser(screenName.value!!)
            }
            show()
        }
    }

    private fun showAccountsDialog(accounts: List<Account>) {
        val myScreenName = (activity.applicationContext as App).account.screenName
        val screenNames = accounts.map {
            if (it.screenName == myScreenName) "@${it.screenName} (now)" else "@${it.screenName}"
        }.toMutableList()
        screenNames.add(activity.getString(R.string.add_account))

        AlertDialog.Builder(activity).setItems(screenNames.toTypedArray()) { _, which ->
            if (screenNames[which].endsWith("(now)")) return@setItems
            if (which == screenNames.lastIndex) {
                activity.startActivity(Intent(activity, AddAccountActivity::class.java))
                return@setItems
            }

            showChangeAccountDialog(accounts[which].screenName)
        }.show()
    }

    private fun showChangeAccountDialog(changeScreenName: String) {
        AlertDialog.Builder(activity).apply {
            setTitle("@${changeScreenName}")
            setPositiveButton(R.string.change_account) { _, _ ->
                viewModel.doChangeUser(changeScreenName)
            }
            setNegativeButton(R.string.delete) { _, _ ->
                viewModel.doDeleteUser(changeScreenName)
            }
            setNeutralButton(R.string.cancel, null)
            show()
        }
    }
}
