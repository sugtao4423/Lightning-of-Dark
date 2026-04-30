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
import sugtao4423.lod.entity.ListSetting
import sugtao4423.lod.utils.showToast
import twitter4j.UserList

class ListSettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val listSettings: List<ListSetting>
        get() = app.account.listSettings

    data class PreferenceSummaryData(
        val selectListSummary: String,
        val loadOnAppStartListSummary: String,
    )

    private val _preferenceSummary = MutableLiveData<PreferenceSummaryData>()
    val preferenceSummary: LiveData<PreferenceSummaryData> = _preferenceSummary

    private val _showChooseListDialog = LiveEvent<List<UserList>>()
    val showChooseListDialog: LiveData<List<UserList>> = _showChooseListDialog

    init {
        setPreferenceSummary()
    }

    fun getChooseListDialogData() = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.getUserLists(app.account.id) }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_list)
            return@launch
        }

        result.let { _showChooseListDialog.value = it }
    }

    fun saveSelectedLists(lists: List<UserList>) = viewModelScope.launch {
        val settings = lists.map { ListSetting(it.id, it.name, false) }
        app.accountRepository.updateListSettings(settings, app.account.id)
        app.reloadAccount()
        setPreferenceSummary()
    }

    fun saveNewListSettings(newSettings: List<ListSetting>) = viewModelScope.launch {
        app.accountRepository.updateListSettings(newSettings, app.account.id)
        app.reloadAccount()
        setPreferenceSummary()
    }

    private fun setPreferenceSummary() {
        val settings = app.account.listSettings
        val listNames = settings.joinToString { it.name }.let {
            app.getString(R.string.param_setting_value_str, it)
        }
        val loadOnAppStartListNames =
            settings.filter { it.loadOnAppStart }.joinToString { it.name }.let {
                app.getString(R.string.param_setting_value_str, it)
            }

        _preferenceSummary.value = PreferenceSummaryData(
            listNames,
            loadOnAppStartListNames
        )
    }

}
