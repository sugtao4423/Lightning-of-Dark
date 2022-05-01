package sugtao4423.lod.ui.settingslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.ResponseList
import twitter4j.UserList

class ListSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    private val myScreenName = app.account.screenName

    val selectedListNames: List<String>
        get() = app.account.selectListNames

    data class PreferenceSummaryData(
        val selectListSummary: String,
        val startAppLoadListSummary: String
    )

    private val _preferenceSummary = MutableLiveData<PreferenceSummaryData>()
    val preferenceSummary: LiveData<PreferenceSummaryData> = _preferenceSummary

    private val _showChooseListDialog = LiveEvent<ResponseList<UserList>>()
    val showChooseListDialog: LiveData<ResponseList<UserList>> = _showChooseListDialog

    init {
        setPreferenceSummary()
    }

    fun getChooseListDialogData() = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.getUserLists(myScreenName) }.getOrNull()
        }
        if (result == null) {
            ShowToast(app, R.string.error_get_list)
            return@launch
        }

        result.let { _showChooseListDialog.value = it }
    }

    fun saveSelectedLists(lists: List<UserList>) = viewModelScope.launch {
        val listNames = lists.map { it.name }
        val listIds = lists.map { it.id }
        app.accountRepository.also {
            it.updateSelectListNames(listNames, myScreenName)
            it.updateSelectListIds(listIds, myScreenName)
        }
        app.reloadAccount()
        setPreferenceSummary()
    }

    fun saveStartAppLoadLists(listNames: List<String>) = viewModelScope.launch {
        app.accountRepository.updateStartAppLoadLists(listNames, myScreenName)
        app.reloadAccount()
        setPreferenceSummary()
    }

    private fun setPreferenceSummary() {
        val sList = app.account.selectListNames.joinToString().let {
            app.getString(R.string.param_setting_value_str, it)
        }
        val sAppLoad = app.account.startAppLoadLists.joinToString().let {
            app.getString(R.string.param_setting_value_str, it)
        }
        _preferenceSummary.value = PreferenceSummaryData(sList, sAppLoad)
    }

}
