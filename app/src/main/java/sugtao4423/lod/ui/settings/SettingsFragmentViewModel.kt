package sugtao4423.lod.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
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
import sugtao4423.lod.utils.showToast
import sugtao4423.twitter4j.UserList
import java.io.File
import java.text.DecimalFormat

data class SettingsUiState(
    val listAsTLChecked: Boolean = false,
    val listAsTLSummary: String? = null,
    val autoLoadTLInterval: Int = 0,
    val cacheSize: String = "",
)

sealed interface SettingsEvent {
    data class ShowSelectListAsTLDialog(val userLists: List<UserList>) : SettingsEvent
}

class SettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        setListAsTLData()
        setAutoLoadTLIntervalSummary()
        setCacheSize()
    }

    fun showSelectListAsTLDialog() = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.getUserLists(app.account.id) }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_list)
            return@launch
        }

        _events.trySend(SettingsEvent.ShowSelectListAsTLDialog(result))
    }

    fun setListAsTL(userList: UserList) = viewModelScope.launch {
        app.accountRepository.updateListAsTL(userList.id, app.account.id)
        app.reloadAccount()
        setListAsTLData()
    }

    fun cancelListAsTL() = viewModelScope.launch {
        app.accountRepository.updateListAsTL(-1, app.account.id)
        app.reloadAccount()
        setListAsTLData()
        setAutoLoadTLIntervalSummary()
    }

    fun cancelListAsTLCancel() = setListAsTLData()

    fun changeAutoLoadTLInterval(interval: Int): Boolean {
        viewModelScope.launch {
            app.accountRepository.updateAutoLoadTLInterval(interval, app.account.id)
            app.reloadAccount()
            setAutoLoadTLIntervalSummary()
        }
        return true
    }

    fun clearCache() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            Glide.get(app.applicationContext).clearDiskCache()
        }
        setCacheSize()
        app.showToast(R.string.cache_deleted)
    }

    private fun setListAsTLData() = _uiState.update {
        val enabled = app.account.listAsTL > 0
        it.copy(
            listAsTLChecked = enabled,
            listAsTLSummary = if (enabled) app.account.listAsTL.toString() else null,
        )
    }

    private fun setAutoLoadTLIntervalSummary() = _uiState.update {
        it.copy(autoLoadTLInterval = app.account.autoLoadTLInterval)
    }

    private fun setCacheSize() {
        fun getDirSize(dir: File): Long = dir.listFiles()?.sumOf {
            when {
                it == null -> 0L
                it.isDirectory -> getDirSize(it)
                it.isFile -> it.length()
                else -> 0L
            }
        } ?: 0L

        val cacheDir = app.applicationContext.cacheDir
        val cacheSize = DecimalFormat("#.# MiB").let {
            it.minimumFractionDigits = 2
            it.maximumFractionDigits = 2
            it.format(getDirSize(cacheDir).toDouble() / 1024 / 1024)
        }
        _uiState.update { it.copy(cacheSize = cacheSize) }
    }

}
