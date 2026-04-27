package sugtao4423.lod.ui.addaccount

import android.app.Application
import android.text.Editable
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
import sugtao4423.lod.entity.Account
import sugtao4423.lod.utils.showToast
import sugtao4423.twitterweb4j.TwitterWeb4j

class AddAccountActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    val isLoading = MutableLiveData(false)
    val enableSaveButton = MutableLiveData(false)

    val ct0Text = MutableLiveData("")
    val authTokenText = MutableLiveData("")
    val userIdText = MutableLiveData("")
    val screenNameText = MutableLiveData("")
    val profileImageUrl = MutableLiveData<String?>(null)

    private val _onFinishEvent = LiveEvent<Unit>()
    val onFinishEvent: LiveData<Unit> = _onFinishEvent

    private val _onStartMainActivityEvent = LiveEvent<Unit>()
    val onStartMainActivityEvent: LiveData<Unit> = _onStartMainActivityEvent

    fun afterChangeCookie(editable: Editable?) {
        val cookieStr = editable?.toString() ?: ""
        val cookie = cookieStr.split(';').mapNotNull {
            val (k, v) = it.trim().split('=', limit = 2).takeIf { p -> p.size == 2 }
                ?: return@mapNotNull null
            k to v
        }.toMap()
        ct0Text.value = cookie["ct0"] ?: ""
        authTokenText.value = cookie["auth_token"] ?: ""
        if (enableSaveButton.value!!) {
            enableSaveButton.value = false
        }
    }

    private fun generateCookie(): String = "ct0=${ct0Text.value}; auth_token=${authTokenText.value}"

    fun getUserInfo() = viewModelScope.launch {
        isLoading.value = true
        val result = withContext(Dispatchers.IO) {
            runCatching {
                TwitterWeb4j(generateCookie()).run {
                    loadClientTransaction()
                    verifyCredentials()
                }
            }.getOrNull()
        }
        isLoading.value = false
        if (result == null) {
            app.showToast(R.string.error_get_user_info)
        }

        val id = result?.id
        val screenName = result?.screenName
        val profileImage = result?.originalProfileImageURLHttps
        if (id != null && !screenName.isNullOrBlank() && !profileImage.isNullOrBlank()) {
            enableSaveButton.value = true
        }

        userIdText.value = id?.toString() ?: ""
        screenNameText.value = screenName ?: ""
        profileImageUrl.value = profileImage
    }

    fun save() = viewModelScope.launch {
        val userId = userIdText.value!!.toLong()
        val screenName = screenNameText.value!!
        val profileImage = profileImageUrl.value!!

        if (app.accountRepository.isExists(userId)) {
            app.showToast(R.string.param_account_already_exists, screenName)
            _onFinishEvent.value = Unit
            return@launch
        }

        val account = Account(userId, screenName, profileImage, generateCookie())
        app.accountRepository.insert(account)

        app.prefRepository.accountId = userId
        app.reloadAccount()
        app.showToast(R.string.success_add_account)
        _onStartMainActivityEvent.value = Unit
        _onFinishEvent.value = Unit
    }

}
