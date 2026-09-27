package sugtao4423.lod.ui.addaccount

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
import sugtao4423.lod.entity.Account
import sugtao4423.lod.utils.showToast
import sugtao4423.twitterweb4j.TwitterWeb4j

data class AddAccountUiState(
    val isLoading: Boolean = false,
    val enableSaveButton: Boolean = false,

    val ct0Text: String = "",
    val authTokenText: String = "",
    val userIdText: String = "",
    val screenNameText: String = "",
    val profileImageUrl: String? = null,
)

sealed interface AddAccountEvent {
    data object Finish : AddAccountEvent
    data object StartMainActivity : AddAccountEvent
}

class AddAccountActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private var editMode = false

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AddAccountEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setEditAccount(id: Long) = viewModelScope.launch {
        val account = app.accountRepository.findById(id)!!
        afterChangeCookie(account.cookie)
        _uiState.update {
            it.copy(
                userIdText = account.id.toString(),
                screenNameText = account.screenName,
                profileImageUrl = account.profileImageUrl,
            )
        }
        editMode = true
    }

    fun afterChangeCookie(cookieString: String) {
        val cookie = cookieString.split(';').mapNotNull {
            val (k, v) = it.trim().split('=', limit = 2).takeIf { p -> p.size == 2 }
                ?: return@mapNotNull null
            k to v
        }.toMap()
        _uiState.update {
            it.copy(
                ct0Text = cookie["ct0"] ?: "",
                authTokenText = cookie["auth_token"] ?: "",
                enableSaveButton = false,
            )
        }
    }

    private fun generateCookie(): String = _uiState.value.let {
        "ct0=${it.ct0Text}; auth_token=${it.authTokenText}"
    }

    fun getUserInfo() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        val result = withContext(Dispatchers.IO) {
            runCatching {
                TwitterWeb4j(generateCookie()).run {
                    loadClientTransaction()
                    verifyCredentials()
                }
            }.getOrNull()
        }
        _uiState.update { it.copy(isLoading = false) }
        if (result == null) {
            app.showToast(R.string.error_get_user_info)
        }

        val id = result?.id
        val screenName = result?.screenName
        val profileImage = result?.profileImage?.originalUrl

        if (editMode && id != _uiState.value.userIdText.toLong()) {
            app.showToast(R.string.error_user_id_mismatch)
            return@launch
        }
        if (id != null && !screenName.isNullOrBlank() && !profileImage.isNullOrBlank()) {
            _uiState.update { it.copy(enableSaveButton = true) }
        }

        _uiState.update {
            it.copy(
                userIdText = id?.toString() ?: "",
                screenNameText = screenName ?: "",
                profileImageUrl = profileImage,
            )
        }
    }

    fun save() = viewModelScope.launch {
        val userId = _uiState.value.userIdText.toLong()
        val screenName = _uiState.value.screenNameText
        val profileImage = _uiState.value.profileImageUrl!!

        if (!editMode && app.accountRepository.isExists(userId)) {
            app.showToast(R.string.param_account_already_exists, screenName)
            _events.trySend(AddAccountEvent.Finish)
            return@launch
        }

        if (editMode) {
            val oldAccount = app.accountRepository.findById(userId)!!
            val account = oldAccount.copy(
                screenName = screenName,
                profileImageUrl = profileImage,
                cookie = generateCookie(),
            )
            app.accountRepository.update(account)
        } else {
            val account = Account(userId, screenName, profileImage, generateCookie())
            app.accountRepository.insert(account)
            app.prefRepository.accountId = userId
        }

        val message = if (editMode) R.string.success_edit_account else R.string.success_add_account
        app.showToast(message)

        if (!app.hasAccount || app.account.id == userId) {
            app.reloadAccount()
            _events.trySend(AddAccountEvent.StartMainActivity)
        }
        _events.trySend(AddAccountEvent.Finish)
    }

}
