package sugtao4423.lod.ui.userpage.fragment

import android.app.Application
import android.graphics.Typeface
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
import sugtao4423.twitter4j.UrlEntity
import sugtao4423.twitter4j.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

data class DetailUiState(
    val bannerUrl: String? = null,
    val iconUrl: String? = null,
    val name: String = "",
    val screenName: String = "",
    val isShowProtected: Boolean = false,

    val isShowRelationship: Boolean = true,
    val myIconUrl: String,
    val relationshipIcon: String = "",

    val bio: String = "",
    val location: String = "",
    val link: String = "",

    val tweetCount: String = "",
    val favoriteCount: String = "",
    val followingCount: String = "",
    val followersCount: String = "",
    val createDate: String = "",
)

sealed interface DetailEvent {
    data class StartBannerImage(val url: String) : DetailEvent
    data class StartIconImage(val url: String) : DetailEvent
    data class StartChrome(val url: String) : DetailEvent
}

class DetailFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()
    private val numberFormat = NumberFormat.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE)

    private var user: User? = null

    fun fontAwesomeTypeface(): Typeface = app.fontAwesomeTypeface

    private val _uiState = MutableStateFlow(DetailUiState(myIconUrl = app.account.profileImageUrl))
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setUser(user: User) {
        this.user = user
        val isShowRelationship = user.id != app.account.id
        _uiState.update {
            it.copy(
                bannerUrl = user.profileBanner?.retinaUrl,
                iconUrl = user.profileImage?.originalUrl,
                name = user.name,
                screenName = "@${user.screenName}",
                isShowProtected = user.isProtected,

                isShowRelationship = isShowRelationship,

                bio = replaceUrlEntities(user.description, user.descriptionUrlEntities),
                location = user.location ?: "",
                link = replaceUrlEntities(user.url, user.urlEntity),

                tweetCount = numberFormat.format(user.statusesCount),
                favoriteCount = numberFormat.format(user.favoritesCount),
                followingCount = numberFormat.format(user.followingCount),
                followersCount = numberFormat.format(user.followersCount),
                createDate = dateFormat.format(user.createdAt),
            )
        }
        if (isShowRelationship) {
            checkRelationShip()
        }
    }

    private fun replaceUrlEntities(target: String?, entity: UrlEntity?): String = when {
        target == null -> ""
        entity == null || entity.expandedUrl == null -> target
        else -> target.replace(entity.url, entity.expandedUrl)
    }

    private fun replaceUrlEntities(target: String?, entity: List<UrlEntity>): String {
        var result = target ?: ""
        entity.forEach { result = replaceUrlEntities(result, it) }
        return result
    }

    private fun checkRelationShip() = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                app.twitter.showFriendship(app.account.id, user!!.id)
            }.getOrNull()
        } ?: return@launch

        val relationshipIcon = when {
            result.isSourceFollowingTarget && result.isSourceFollowedByTarget -> app.getString(R.string.icon_followEach)
            result.isSourceFollowingTarget -> app.getString(R.string.icon_followFollow)
            result.isSourceFollowedByTarget -> app.getString(R.string.icon_followFollower)
            result.isSourceBlockedByTarget || result.isSourceBlockingTarget -> app.getString(R.string.icon_followBlock)
            else -> ""
        }
        _uiState.update { it.copy(relationshipIcon = relationshipIcon) }
    }

    fun onClickBanner(): Boolean {
        user?.profileBanner?.size1500x500Url?.let { _events.trySend(DetailEvent.StartBannerImage(it)) }
        return true
    }

    fun onLongClickBanner(): Boolean {
        user?.profileBanner?.size1500x500Url?.let { _events.trySend(DetailEvent.StartChrome(it)) }
        return true
    }

    fun onClickIcon(): Boolean {
        user?.profileImage?.originalUrl?.let { _events.trySend(DetailEvent.StartIconImage(it)) }
        return true
    }

    fun onLongClickIcon(): Boolean {
        user?.profileImage?.originalUrl?.let { _events.trySend(DetailEvent.StartChrome(it)) }
        return true
    }

}
