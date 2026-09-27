package sugtao4423.lod.ui.userpage

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
import sugtao4423.lod.utils.showToast
import sugtao4423.twitter4j.User

data class UserPageUiState(
    val user: User? = null,
    val actionBarTitle: String = "",
)

sealed interface UserPageEvent {
    data object Finish : UserPageEvent
}

class UserPageActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private val _uiState = MutableStateFlow(UserPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<UserPageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setUser(user: User) {
        if (_uiState.value.user != null) return
        _uiState.update {
            it.copy(user = user, actionBarTitle = user.name)
        }
    }

    fun setUser(screenName: String) {
        if (_uiState.value.user != null) return
        loadUser(screenName)
    }

    private fun loadUser(screenName: String) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.showUser(screenName) }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_user_detail)
            _events.trySend(UserPageEvent.Finish)
            return@launch
        }

        _uiState.update {
            it.copy(user = result, actionBarTitle = result.name)
        }
    }

}
