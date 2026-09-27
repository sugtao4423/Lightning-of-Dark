package sugtao4423.lod.ui.settingslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.entity.ListSetting
import sugtao4423.lod.utils.showToast
import sugtao4423.twitter4j.UserList

data class ListSettingsUiState(
    val selectListSummary: String = "",
    val loadOnAppStartListSummary: String = "",
)

sealed interface ListSettingsEvent {
    data class ShowChooseListDialog(val lists: List<UserList>) : ListSettingsEvent
}

class ListSettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val listSettings: List<ListSetting>
        get() = app.account.listSettings

    private val _uiState = MutableStateFlow(ListSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ListSettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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

        _events.trySend(ListSettingsEvent.ShowChooseListDialog(result))
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

        _uiState.update {
            it.copy(
                selectListSummary = listNames,
                loadOnAppStartListSummary = loadOnAppStartListNames,
            )
        }
    }

}
