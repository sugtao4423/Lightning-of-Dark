package sugtao4423.lod.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.showToast
import twitter4j.ResponseList
import twitter4j.UserList
import java.io.File
import java.text.DecimalFormat

class SettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    data class ListAsTLData(
        val isChecked: Boolean,
        val summary: String?
    )

    private val _listAsTLData = MutableLiveData<ListAsTLData>()
    val listAsTLData: LiveData<ListAsTLData> = _listAsTLData

    private val _onShowSelectListAsTLDialog = LiveEvent<ResponseList<UserList>>()
    val onShowSelectListAsTLDialog: LiveData<ResponseList<UserList>> = _onShowSelectListAsTLDialog

    private val _autoLoadTLInterval = MutableLiveData<Int>()
    val autoLoadTLInterval: LiveData<Int> = _autoLoadTLInterval

    private val _cacheSizeMB = MutableLiveData<String>()
    val cacheSizeMB: LiveData<String> = _cacheSizeMB

    init {
        setListAsTLData()
        setAutoLoadTLIntervalSummary()
        setCacheSize()
    }

    fun showSelectListAsTLDialog() = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.getUserLists(app.twitter.screenName) }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_list)
            return@launch
        }

        result.let { _onShowSelectListAsTLDialog.value = it }
    }

    fun setListAsTL(userList: UserList) = viewModelScope.launch {
        app.accountRepository.updateListAsTL(userList.id, app.account.screenName)
        app.reloadAccount()
        setListAsTLData()
    }

    fun cancelListAsTL() = viewModelScope.launch {
        app.prefRepository.autoLoadTLInterval = 0
        app.accountRepository.updateListAsTL(-1, app.account.screenName)
        app.accountRepository.updateAutoLoadTLInterval(0, app.account.screenName)
        app.reloadAccount()
        setListAsTLData()
        setAutoLoadTLIntervalSummary()
    }

    fun cancelListAsTLCancel() = setListAsTLData()

    fun changeAutoLoadTLInterval(interval: Int): Boolean {
        val isListAsTL = app.account.listAsTL > 0
        if (!isListAsTL && interval > 0 && interval < 60) {
            app.showToast(R.string.error_auto_load_tl_interval)
            return false
        }

        viewModelScope.launch {
            app.accountRepository.updateAutoLoadTLInterval(interval, app.account.screenName)
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

    private fun setListAsTLData() {
        _listAsTLData.value = ListAsTLData(
            app.account.listAsTL > 0,
            if (app.account.listAsTL > 0) app.account.listAsTL.toString() else null
        )
    }

    private fun setAutoLoadTLIntervalSummary() {
        _autoLoadTLInterval.value = app.account.autoLoadTLInterval
    }

    private fun setCacheSize() {
        fun getDirSize(dir: File): Long {
            var size = 0L
            dir.listFiles()?.map {
                when {
                    it == null -> return@map
                    it.isDirectory -> size += getDirSize(it)
                    it.isFile -> size += it.length()
                }
            }
            return size
        }

        val cacheDir = app.applicationContext.cacheDir
        val cacheSize = DecimalFormat("#.#").let {
            it.minimumFractionDigits = 2
            it.maximumFractionDigits = 2
            it.format(getDirSize(cacheDir).toDouble() / 1024 / 1024)
        }
        _cacheSizeMB.value = cacheSize
    }

}
