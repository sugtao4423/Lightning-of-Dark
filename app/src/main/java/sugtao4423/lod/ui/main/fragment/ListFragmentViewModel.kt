package sugtao4423.lod.ui.main.fragment

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.entity.ListSetting
import sugtao4423.lod.ui.BaseTweetListViewModel
import sugtao4423.lod.utils.showToast

class ListFragmentViewModel(application: Application) : BaseTweetListViewModel(application) {

    private lateinit var listSetting: ListSetting

    var listIndex: Int = -1
        set(value) {
            if (field != value) {
                field = value
                listSetting = app.account.listSettings[value]
                if (listSetting.loadOnAppStart) {
                    loadList()
                }
            }
        }

    override fun loadList(isRefresh: Boolean) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                app.twitter.listTweetsTimeline(listSetting.id, tweetCount, bottomCursor)
            }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_list)
            return@launch
        }

        if (result.isNotEmpty()) {
            bottomCursor = result.cursorBottom
        }
        hasNextPage = result.isNotEmpty()
        result.let { addStatuses.value = it }
    }

}
