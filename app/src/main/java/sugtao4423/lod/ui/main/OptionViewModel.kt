package sugtao4423.lod.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.entity.Account
import sugtao4423.lod.utils.showToast
import sugtao4423.twitterweb4j.model.CreateTweet
import java.text.NumberFormat

sealed interface OptionEvent {
    data class SearchUser(val screenName: String) : OptionEvent
    data class GetAllAccounts(val accounts: List<Account>) : OptionEvent
    data object RestartMainActivity : OptionEvent
    data class ShowLevelInfoDialog(val message: String) : OptionEvent
    data class ShowUseInfoDialog(val message: String) : OptionEvent
}

class OptionViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private val _events = Channel<OptionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun doBombTweet(staticText: String, loopText: String, loopCount: String) {
        if (loopCount.isEmpty()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var loop = ""
                for (i in 0 until loopCount.toInt()) {
                    loop += loopText
                    val createTweet = CreateTweet(staticText + loop)
                    runCatching { app.twitter.createTweet(createTweet) }
                }
            }
            app.showToast(R.string.param_success_tweet, 0)
        }
    }

    fun doSearchUser(screenName: String) {
        if (screenName.isEmpty()) {
            app.showToast(R.string.edittext_empty)
            return
        }

        val sn = screenName.replace("@", "")
        _events.trySend(OptionEvent.SearchUser(sn))
    }

    fun doGetAllAccounts() = viewModelScope.launch {
        val accounts = app.accountRepository.getAll()
        _events.trySend(OptionEvent.GetAllAccounts(accounts))
    }

    fun doChangeUser(id: Long) {
        if (app.account.id == id) return
        app.prefRepository.accountId = id
        _events.trySend(OptionEvent.RestartMainActivity)
    }

    fun doDeleteUser(id: Long, screenName: String) = viewModelScope.launch {
        app.accountRepository.delete(id)
        app.showToast(R.string.param_success_account_delete, screenName)
    }

    fun showLevelInfo() {
        val nf = NumberFormat.getInstance()
        val lv = app.levelRepository
        val level = nf.format(lv.getLevel())
        val nextExp = nf.format(lv.getNextExp())
        val totalExp = nf.format(lv.getTotalExp())
        val message = app.getString(R.string.param_next_level_total_exp, level, nextExp, totalExp)
        _events.trySend(OptionEvent.ShowLevelInfoDialog(message))
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
        _events.trySend(OptionEvent.ShowUseInfoDialog(message))
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
