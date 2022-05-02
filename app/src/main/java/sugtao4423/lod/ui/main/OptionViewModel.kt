package sugtao4423.lod.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.ShowToast
import sugtao4423.lod.entity.Account
import java.text.NumberFormat

class OptionViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private val _onSearchUserScreenName = LiveEvent<String>()
    val onSearchUserScreenName: LiveData<String> = _onSearchUserScreenName

    private val _onGetAllAccounts = LiveEvent<List<Account>>()
    val onGetAllAccounts: LiveEvent<List<Account>> = _onGetAllAccounts

    private val _onRestartMainActivity = LiveEvent<Unit>()
    val onRestartMainActivity: LiveData<Unit> = _onRestartMainActivity

    private val _onShowLevelInfoDialog = LiveEvent<String>()
    val onShowLevelInfoDialog: LiveData<String> = _onShowLevelInfoDialog

    private val _onShowUseInfoDialog = LiveEvent<String>()
    val onShowUseInfoDialog: LiveData<String> = _onShowUseInfoDialog

    fun doBombTweet(staticText: String, loopText: String, loopCount: String) {
        if (loopCount.isEmpty()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var loop = ""
                for (i in 0 until loopCount.toInt()) {
                    loop += loopText
                    runCatching { app.twitter.updateStatus(staticText + loop) }
                }
            }
            ShowToast(app, R.string.param_success_tweet, 0)
        }
    }

    fun doSearchUser(screenName: String) {
        if (screenName.isEmpty()) {
            ShowToast(app, R.string.edittext_empty)
            return
        }

        _onSearchUserScreenName.value = screenName.replace("@", "")
    }

    fun doGetAllAccounts() = viewModelScope.launch {
        _onGetAllAccounts.value = app.accountRepository.getAll()
    }

    fun doChangeUser(changedUserScreenName: String) {
        app.prefRepository.screenName = changedUserScreenName
        _onRestartMainActivity.value = Unit
    }

    fun doDeleteUser(deleteUserScreenName: String) = viewModelScope.launch {
        app.accountRepository.delete(deleteUserScreenName)
        ShowToast(app, R.string.param_success_account_delete, deleteUserScreenName)
    }

    fun showLevelInfo() {
        val nf = NumberFormat.getInstance()
        val lv = app.levelRepository
        val level = nf.format(lv.getLevel())
        val nextExp = nf.format(lv.getNextExp())
        val totalExp = nf.format(lv.getTotalExp())
        val message = app.getString(R.string.param_next_level_total_exp, level, nextExp, totalExp)
        _onShowLevelInfoDialog.value = message
    }

    fun showUseTimeInfo() = viewModelScope.launch {
        val repo = app.useTimeRepository
        val todayUse = repo.getTodayUseTimeInMillis()
        val yesterdayUse = repo.getYesterdayUseTimeInMillis()
        val last30daysUse = repo.getLastNDaysUseTimeInMillis(30)
        val totalUse = repo.getTotalUseTimeInMillis()
        val startDate = repo.getRecordStartDate()
        val message = app.getString(
            R.string.param_use_info_text,
            milliTime2Str(todayUse),
            milliTime2Str(yesterdayUse),
            milliTime2Str(last30daysUse),
            milliTime2Str(totalUse),
            startDate
        )
        _onShowUseInfoDialog.value = message
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

    private fun zeroPad(i: Int): String = if (i < 10) "0$i" else i.toString()
}
