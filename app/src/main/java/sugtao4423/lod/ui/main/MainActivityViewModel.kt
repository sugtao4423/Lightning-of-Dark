package sugtao4423.lod.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent
import sugtao4423.lod.App
import sugtao4423.lod.entity.ListSetting
import sugtao4423.lod.service.AutoLoadTLService
import sugtao4423.twitterweb4j.model.CursorList
import twitter4j.Status

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    val hasAccount = app.hasAccount
    val listSettings: List<ListSetting>
        get() = app.account.listSettings

    private val _onStartAutoLoadTLService = LiveEvent<Unit>()
    val onStartAutoLoadTLService = _onStartAutoLoadTLService

    private val _onNewStatuses = LiveEvent<CursorList<Status>>()
    val onNewStatuses: LiveData<CursorList<Status>> = _onNewStatuses

    private val _onNewMention = LiveEvent<List<Status>>()
    val onNewMention: LiveData<List<Status>> = _onNewMention

    private var kickedInitialized = false
    fun viewInitialized() {
        if (kickedInitialized || app.account.autoLoadTLInterval == 0) {
            return
        }

        app.autoLoadTLListener = object : AutoLoadTLService.AutoLoadTLListener {
            override fun onStatus(statuses: CursorList<Status>) {
                if (statuses.isEmpty()) return
                app.cursorTop = statuses.cursorTop
                _onNewStatuses.postValue(statuses)
                _onNewMention.postValue(statuses.filter {
                    app.mentionPattern.matcher(it.text).find() && !it.isRetweet
                })

            }
        }
        _onStartAutoLoadTLService.value = Unit
        kickedInitialized = true
    }

}
