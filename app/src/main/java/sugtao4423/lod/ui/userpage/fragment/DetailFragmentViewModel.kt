package sugtao4423.lod.ui.userpage.fragment

import android.app.Application
import android.graphics.Typeface
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
import sugtao4423.lod.utils.showToast
import twitter4j.User

class DetailFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    fun isShowRelationship(user: User?): Boolean = user?.screenName != app.account.screenName

    fun fontAwesomeTypeface(): Typeface = app.fontAwesomeTypeface

    private val _relationshipIcon = MutableLiveData<String>()
    val relationshipIcon: LiveData<String> = _relationshipIcon

    data class RelationshipIconUrls(
        val me: String,
        val target: String,
    )

    private val _relationShipIconUrls = MutableLiveData<RelationshipIconUrls>()
    val relationShipIconUrls: LiveData<RelationshipIconUrls> = _relationShipIconUrls

    private val _onStartIconImageUrl = LiveEvent<String>()
    val onStartIconImageUrl: LiveData<String> = _onStartIconImageUrl

    private val _onStartBannerImageUrl = LiveEvent<String>()
    val onStartBannerImageUrl: LiveData<String> = _onStartBannerImageUrl

    private val _onStartChromeUrl = LiveEvent<String>()
    val onStartChromeUrl: LiveData<String> = _onStartChromeUrl

    fun checkRelationShip(user: User) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                app.twitter.showFriendship(app.account.screenName, user.screenName)
            }.getOrNull()
        }
        if (result != null) {
            _relationshipIcon.value = when {
                result.isSourceFollowingTarget && result.isSourceFollowedByTarget -> app.getString(R.string.icon_followEach)
                result.isSourceFollowingTarget -> app.getString(R.string.icon_followFollow)
                result.isSourceFollowedByTarget -> app.getString(R.string.icon_followFollower)
                result.isSourceBlockingTarget -> app.getString(R.string.icon_followBlock)
                else -> ""
            }
        }
    }

    fun getRelationshipIconUrls(user: User) = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                Pair(
                    app.twitter.verifyCredentials().biggerProfileImageURLHttps,
                    user.biggerProfileImageURLHttps
                )
            }.getOrNull()
        }
        if (result == null) {
            app.showToast(R.string.error_get_user_icon)
            return@launch
        }

        _relationShipIconUrls.value = RelationshipIconUrls(result.first, result.second)
    }

    fun onClickIcon(user: User?): Boolean = user?.originalProfileImageURLHttps.let {
        it?.let { url -> _onStartIconImageUrl.value = url }
        true
    }

    fun onLongClickIcon(user: User?): Boolean = user?.originalProfileImageURLHttps.let {
        it?.let { url -> _onStartChromeUrl.value = url }
        true
    }

    fun onClickBanner(user: User?): Boolean = user?.profileBanner1500x500URL.let {
        it?.let { url -> _onStartBannerImageUrl.value = url }
        true
    }

    fun onLongClickBanner(user: User?): Boolean = user?.profileBanner1500x500URL.let {
        it?.let { url -> _onStartChromeUrl.value = url }
        true
    }

}
