package sugtao4423.lod.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import sugtao4423.lod.App
import sugtao4423.lod.entity.ListSetting
import sugtao4423.lod.service.AutoLoadTLService
import sugtao4423.twitter4j.Status
import sugtao4423.twitterweb4j.model.CursorList

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    val hasAccount = app.hasAccount
    val listSettings: List<ListSetting>
        get() = app.account.listSettings

    private val _onStartAutoLoadTLService = Channel<Unit>(Channel.BUFFERED)
    val onStartAutoLoadTLService = _onStartAutoLoadTLService.receiveAsFlow()

    private val _onNewStatuses = Channel<CursorList<Status>>(Channel.BUFFERED)
    val onNewStatuses = _onNewStatuses.receiveAsFlow()

    private val _onNewMention = Channel<List<Status>>(Channel.BUFFERED)
    val onNewMention = _onNewMention.receiveAsFlow()

    private var kickedInitialized = false
    fun viewInitialized() {
        if (kickedInitialized || app.account.autoLoadTLInterval == 0) {
            return
        }

        app.autoLoadTLListener = object : AutoLoadTLService.AutoLoadTLListener {
            override fun onStatus(statuses: CursorList<Status>) {
                if (statuses.isEmpty()) return
                app.cursorTop = statuses.cursorTop
                _onNewStatuses.trySend(statuses)
                statuses.filter {
                    app.mentionPattern.matcher(it.text).find() && !it.isRetweet
                }.takeIf { it.isNotEmpty() }?.let {
                    _onNewMention.trySend(it)
                }
            }
        }
        _onStartAutoLoadTLService.trySend(Unit)
        kickedInitialized = true
    }

}
