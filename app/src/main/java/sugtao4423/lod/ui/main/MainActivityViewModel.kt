package sugtao4423.lod.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent
import sugtao4423.lod.App
import sugtao4423.lod.AutoLoadTLService
import twitter4j.ResponseList
import twitter4j.Status

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    data class ListData(val id: Long, val name: String, val isAppStartLoad: Boolean)

    private val app = getApplication<App>()
    val hasAccount = app.hasAccount
    val listData: List<ListData>
        get() {
            val result = arrayListOf<ListData>()
            for (i in app.account.selectListNames.indices) {
                val id = app.account.selectListIds[i]
                val name = app.account.selectListNames[i]
                val isAppStartLoad = app.account.startAppLoadLists.contains(name)
                result.add(ListData(id, name, isAppStartLoad))
            }
            return result.toList()
        }

    private val _onStartTweetActivity = LiveEvent<Unit>()
    val onStartTweetActivity: LiveData<Unit> = _onStartTweetActivity

    private val _showOptionDialog = LiveEvent<Unit>()
    val showOptionDialog: LiveData<Unit> = _showOptionDialog

    private val _onStartAutoLoadTLService = LiveEvent<Unit>()
    val onStartAutoLoadTLService = _onStartAutoLoadTLService

    private val _onNewStatuses = LiveEvent<ResponseList<Status>>()
    val onNewStatuses: LiveData<ResponseList<Status>> = _onNewStatuses

    private val _onNewMention = LiveEvent<List<Status>>()
    val onNewMention: LiveData<List<Status>> = _onNewMention

    fun clickNewTweet() {
        _onStartTweetActivity.value = Unit
    }

    fun clickOption() {
        _showOptionDialog.value = Unit
    }

    private var kickedInitialized = false
    fun viewInitialized() {
        if (kickedInitialized || app.account.autoLoadTLInterval == 0) {
            return
        }

        app.autoLoadTLListener = object : AutoLoadTLService.AutoLoadTLListener {
            override fun onStatus(statuses: ResponseList<Status>) {
                if (statuses.isEmpty()) return
                app.latestTweetId = statuses.first().id
                _onNewStatuses.value = statuses
                _onNewMention.value = statuses.filter {
                    app.mentionPattern.matcher(it.text).find() && !it.isRetweet
                }
            }
        }
        _onStartAutoLoadTLService.value = Unit
        kickedInitialized = true
    }

}
