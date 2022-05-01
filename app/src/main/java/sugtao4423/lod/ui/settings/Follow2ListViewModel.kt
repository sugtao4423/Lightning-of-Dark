package sugtao4423.lod.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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

class Follow2ListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private val _toggleLoadingDialog = LiveEvent<Unit>()
    val toggleLoadingDialog: LiveData<Unit> = _toggleLoadingDialog

    private val _showSelectSyncList = LiveEvent<ResponseList<UserList>>()
    val showSelectSyncList: LiveData<ResponseList<UserList>> = _showSelectSyncList

    private val _showSyncListResultMessage = LiveEvent<Int>()
    val showSyncListResultMessage: LiveData<Int> = _showSyncListResultMessage

    fun createFollowSyncList() {
        val unixTime = System.currentTimeMillis() / 1000
        val listName = "home_timeline-$unixTime"
        val listIsPublic = false
        val listDescription = "user count = follow + me"
        _toggleLoadingDialog.value = Unit

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    app.twitter.createUserList(
                        listName,
                        listIsPublic,
                        listDescription
                    )
                }.getOrNull()
            }
            _toggleLoadingDialog.value = Unit
            if (result == null) {
                ShowToast(app, R.string.error_create_list)
                return@launch
            }

            syncFollowList(result)
        }
    }

    fun selectFollowSyncList() = viewModelScope.launch {
        _toggleLoadingDialog.value = Unit

        val result = withContext(Dispatchers.IO) {
            runCatching { app.twitter.getUserLists(app.twitter.screenName) }.getOrNull()
        }
        _toggleLoadingDialog.value = Unit
        if (result == null) {
            ShowToast(app, R.string.error_get_list)
            return@launch
        }

        result.let { _showSelectSyncList.value = it }
    }

    fun followSyncListSelected(list: UserList) = syncFollowList(list)

    private fun syncFollowList(list: UserList) = viewModelScope.launch {
        _toggleLoadingDialog.value = Unit

        val result = withContext(Dispatchers.IO) {
            runCatching {
                deleteAllUsersInList(list)
                addAllFriendsInList(list)
            }.getOrNull()
        }
        _toggleLoadingDialog.value = Unit

        _showSyncListResultMessage.value = if (result == null) {
            R.string.error_follow2list
        } else {
            R.string.success_follow2list
        }
    }

    private fun deleteAllUsersInList(list: UserList) {
        val userIds = app.twitter.getUserListMembers(list.id, 5000, -1).map { it.id }
        if (userIds.isNotEmpty()) {
            app.twitter.destroyUserListMembers(list.id, userIds.toLongArray())
        }
    }

    private fun addAllFriendsInList(list: UserList) {
        val friendIds = app.twitter.getFriendsIDs(-1).iDs + app.twitter.verifyCredentials().id
        friendIds.toList().chunked(100).forEach {
            app.twitter.createUserListMembers(list.id, *it.toLongArray())
        }
    }

}
