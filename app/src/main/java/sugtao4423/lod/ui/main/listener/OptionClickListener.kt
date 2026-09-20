package sugtao4423.lod.ui.main.listener

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AlertDialog
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
        val binding = OptionDialogTweetBombBinding.inflate(activity.layoutInflater)
        AlertDialog.Builder(activity).apply {
            setView(binding.root)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.doBombTweet(
                    binding.staticTextEdit.text.toString(),
                    binding.loopTextEdit.text.toString(),
                    binding.loopCountEdit.text.toString()
                )
            }
            show()
        }
    }

    private fun searchUser() {
        val binding = OptionDialogSearchUserBinding.inflate(activity.layoutInflater)
        AlertDialog.Builder(activity).apply {
            setView(binding.root)
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.doSearchUser(binding.screenNameEdit.text.toString())
            }
            show()
        }
    }

    private fun showAccountsDialog(accounts: List<Account>) {
        val screenNames =
            accounts.map { "@${it.screenName}" } + activity.getString(R.string.add_account)
        val myId = (activity.applicationContext as App).account.id
        val currentIndex = accounts.indexOfFirst { it.id == myId }

        AlertDialog.Builder(activity)
            .setSingleChoiceItems(screenNames.toTypedArray(), currentIndex) { dialog, which ->
                if (which == screenNames.lastIndex) {
                    activity.startActivity(Intent(activity, AddAccountActivity::class.java))
                    return@setSingleChoiceItems
                }

                showChangeAccountDialog(accounts[which])
                dialog.dismiss()
            }.show()
    }

    private fun showChangeAccountDialog(account: Account) {
        AlertDialog.Builder(activity).apply {
            setTitle("@${account.screenName}")
            setPositiveButton(R.string.change_account) { _, _ ->
                viewModel.doChangeUser(account.id)
            }
            setNegativeButton(R.string.edit) { _, _ ->
                val intent = Intent(activity, AddAccountActivity::class.java).apply {
                    putExtra(AddAccountActivity.INTENT_KEY_EDIT_ACCOUNT_ID, account.id)
                }
                activity.startActivity(intent)
            }
            setNeutralButton(R.string.delete) { _, _ ->
                viewModel.doDeleteUser(account.id, account.screenName)
            }
            show()
        }
    }
}
