package sugtao4423.lod

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.ui.addaccount.AddAccountActivity
import sugtao4423.lod.ui.main.MainActivity
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.utils.Utils
import twitter4j.TwitterException
import java.text.NumberFormat

class OptionClickListener(private val context: Context) : DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            0 -> tweetBomb()
            1 -> searchUser()
            2 -> accountSelect()
            3 -> levelInfo()
            4 -> useInfo()
            5 -> context.startActivity(Intent(context, Settings::class.java))
        }
    }

    private fun tweetBomb() {
        val bombView = View.inflate(context, R.layout.tweet_bomb, null)
        AlertDialog.Builder(context).apply {
            setView(bombView)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ ->
                val staticText = (bombView.findViewById<TextInputEditText>(R.id.bombStaticText)).text.toString()
                val loopText = (bombView.findViewById<TextInputEditText>(R.id.bombLoopText)).text.toString()
                val loopCountStr = (bombView.findViewById<TextInputEditText>(R.id.bombLoopCount)).text.toString()
                if (loopCountStr.isEmpty()) {
                    return@setPositiveButton
                }
                val loopCount = loopCountStr.toInt()

                CoroutineScope(Dispatchers.IO).launch {
                    var loop = ""
                    for (i in 0 until loopCount) {
                        loop += loopText
                        try {
                            (context.applicationContext as App).twitter.updateStatus(staticText + loop)
                        } catch (e: TwitterException) {
                        }
                    }
                    withContext(Dispatchers.Main) {
                        ShowToast(context.applicationContext, R.string.param_success_tweet, 0)
                    }
                }
            }
            show()
        }
    }

    private fun searchUser() {
        val userEdit = EditText(context)
        val editContainer = FrameLayout(context)
        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).also {
            val margin = Utils.convertDpToPx(context, 24)
            it.leftMargin = margin
            it.rightMargin = margin
            userEdit.layoutParams = it
        }
        editContainer.addView(userEdit)

        AlertDialog.Builder(context).apply {
            setMessage(R.string.input_users_screen_name)
            setView(editContainer)
            setPositiveButton(R.string.ok) { _, _ ->
                val userScreenName = userEdit.text.toString()
                if (userScreenName.isEmpty()) {
                    ShowToast(context.applicationContext, R.string.edittext_empty)
                } else {
                    val userPage = Intent(context, UserPageActivity::class.java)
                    userPage.putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_SCREEN_NAME, userScreenName.replace("@", ""))
                    context.startActivity(userPage)
                }
            }
            show()
        }
    }

    private fun accountSelect() {
        val app = context.applicationContext as App
        val myScreenName = app.account.screenName
        CoroutineScope(Dispatchers.Main).launch {
            val accounts = app.accountRepository.getAll()
            val screenNames = arrayListOf<String>()
            accounts.map {
                if (it.screenName == myScreenName) {
                    screenNames.add("@${it.screenName} (now)")
                } else {
                    screenNames.add("@${it.screenName}")
                }
            }
            screenNames.add(context.getString(R.string.add_account))
            AlertDialog.Builder(context).also { selectDialog ->
                selectDialog.setItems(screenNames.toTypedArray()) { _, which ->
                    val selected = screenNames[which]
                    if (selected == context.getString(R.string.add_account)) {
                        context.startActivity(Intent(context, AddAccountActivity::class.java))
                    } else if (selected != "@$myScreenName (now)") {
                        AlertDialog.Builder(context).also { confirmDialog ->
                            confirmDialog.setTitle(selected)
                            confirmDialog.setPositiveButton(R.string.change_account) { _, _ ->
                                app.prefRepository.screenName = accounts[which].screenName
                                (context as MainActivity).restart()
                            }
                            confirmDialog.setNegativeButton(R.string.delete) { _, _ ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    app.accountRepository.delete(accounts[which].screenName)
                                }
                                ShowToast(context.applicationContext, R.string.param_success_account_delete, accounts[which].screenName)
                            }
                            confirmDialog.setNeutralButton(R.string.cancel, null)
                            confirmDialog.show()
                        }
                    }
                }
                selectDialog.show()
            }
        }
    }

    private fun levelInfo() {
        val lv = (context.applicationContext as App).levelRepository
        NumberFormat.getInstance().apply {
            val level = format(lv.getLevel())
            val nextExp = format(lv.getNextExp())
            val totalExp = format(lv.getTotalExp())
            val message = context.getString(R.string.param_next_level_total_exp, level, nextExp, totalExp)
            AlertDialog.Builder(context).setMessage(message).setPositiveButton(R.string.ok, null).show()
        }
    }

    private fun useInfo() {
        val repo = (context.applicationContext as App).useTimeRepository
        CoroutineScope(Dispatchers.Main).launch {
            val todayUse = repo.getTodayUseTimeInMillis()
            val yesterdayUse = repo.getYesterdayUseTimeInMillis()
            val last30daysUse = repo.getLastNDaysUseTimeInMillis(30)
            val totalUse = repo.getTotalUseTimeInMillis()
            val startDate = repo.getRecordStartDate()
            val message = context.getString(
                R.string.param_use_info_text,
                milliTime2Str(todayUse),
                milliTime2Str(yesterdayUse),
                milliTime2Str(last30daysUse),
                milliTime2Str(totalUse),
                startDate
            )
            AlertDialog.Builder(context).apply {
                setTitle(R.string.use_info)
                setMessage(message)
                setPositiveButton(R.string.ok, null)
                show()
            }
        }
    }

    private fun milliTime2Str(time: Long): String {
        val day = (time / 1000 / 86400).toInt()
        val hour = ((time / 1000 - day * 86400) / 3600).toInt()
        val minute = ((time / 1000 - day * 86400 - hour * 3600) / 60).toInt()
        val second = (time / 1000 - day * 86400 - hour * 3600 - minute * 60).toInt()

        var result = if (day != 0) {
            "$day days, "
        } else {
            ""
        }
        result += zeroPad(hour) + ":" + zeroPad(minute) + ":" + zeroPad(second)
        return result
    }

    private fun zeroPad(i: Int): String {
        return if (i < 10) {
            "0$i"
        } else {
            i.toString()
        }
    }

}
