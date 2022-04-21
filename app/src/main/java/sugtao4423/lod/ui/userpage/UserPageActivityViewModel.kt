package sugtao4423.lod.ui.userpage

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
import twitter4j.User

class UserPageActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private val _actionBarTitle = MutableLiveData("")
    val actionBarTitle: LiveData<String> = _actionBarTitle

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _onFinish = LiveEvent<Unit>()
    val onFinish: LiveData<Unit> = _onFinish

    fun setUser(user: User) {
        if (_user.value == null) {
            _user.value = user
            _actionBarTitle.value = user.name
        }
    }

    fun setUser(screenName: String) {
        if (_user.value == null) {
            loadUser(screenName)
        }
    }

    private fun loadUser(screenName: String) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.showUser(screenName) }.getOrNull()
        }
        if (result == null) {
            ShowToast(app, R.string.error_get_user_detail)
            _onFinish.value = Unit
            return@launch
        }

        result.let { _user.value = it }
        _actionBarTitle.value = result.name
    }

}
