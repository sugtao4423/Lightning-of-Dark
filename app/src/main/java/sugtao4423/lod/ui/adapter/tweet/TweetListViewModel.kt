package sugtao4423.lod.ui.adapter.tweet

import android.content.Intent
import android.view.View
import sugtao4423.lod.App
import sugtao4423.lod.ui.showmedia.ShowMediaActivity
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.utils.original
import sugtao4423.twitter4j.MediaEntity
import sugtao4423.twitter4j.Status

class TweetListViewModel(app: App) {

    val protectIconSize = app.prefRepository.userNameFontSize - 3
    val nameTextSize = app.prefRepository.userNameFontSize
    val textSize = app.prefRepository.contentFontSize
    val dateTextSize = app.prefRepository.dateFontSize
    val isShowMilliSeconds = app.prefRepository.isMillisecond

    fun tweetMediaAdapter(status: Status) = TweetMediaListAdapter(this).apply {
        submitList(status.original().mediaEntities)
    }

    fun onClickUserIcon(view: View, status: Status) {
        val intent = Intent(view.context, UserPageActivity::class.java).apply {
            putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_OBJECT, status.original().user)
        }
        view.context.startActivity(intent)
    }

    fun onClickMedia(view: View, mediaEntities: List<MediaEntity>, tappedIndex: Int) {
        val mediaArrayList = ArrayList(mediaEntities)
        val intent = Intent(view.context, ShowMediaActivity::class.java).apply {
            putExtra(ShowMediaActivity.INTENT_EXTRA_KEY_MEDIA_ENTITIES, mediaArrayList)
            putExtra(ShowMediaActivity.INTENT_EXTRA_KEY_POSITION, tappedIndex)
        }
        view.context.startActivity(intent)
    }

}
