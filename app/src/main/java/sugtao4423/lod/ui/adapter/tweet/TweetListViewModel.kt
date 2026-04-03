package sugtao4423.lod.ui.adapter.tweet

import android.content.Intent
import android.view.View
import sugtao4423.lod.App
import sugtao4423.lod.ui.adapter.converter.TweetListConverter
import sugtao4423.lod.ui.showimage.ShowImageActivity
import sugtao4423.lod.ui.showvideo.ShowVideoActivity
import sugtao4423.lod.ui.userpage.UserPageActivity
import twitter4j.MediaEntity
import twitter4j.Status

class TweetListViewModel(app: App) {

    val fontAwesomeTypeface = app.fontAwesomeTypeface
    val protectIconSize = app.prefRepository.userNameFontSize - 3
    val nameTextSize = app.prefRepository.userNameFontSize
    val textSize = app.prefRepository.contentFontSize
    val dateTextSize = app.prefRepository.dateFontSize
    val isShowMilliSeconds = app.prefRepository.isMillisecond

    fun tweetMediaAdapter(status: Status) = TweetMediaListAdapter(this).apply {
        val original = TweetListConverter.originalStatus(status)
        submitList(original!!.mediaEntities.toList())
    }

    fun onClickUserIcon(view: View, status: Status) {
        val intent = Intent(view.context, UserPageActivity::class.java).apply {
            putExtra(
                UserPageActivity.INTENT_EXTRA_KEY_USER_OBJECT,
                TweetListConverter.originalStatus(status)!!.user
            )
        }
        view.context.startActivity(intent)
    }

    fun onClickMediaImage(view: View, mediaEntities: List<MediaEntity>, tappedIndex: Int) {
        val allImages = TweetListConverter.allImageUrls(mediaEntities)

        val intent = Intent(view.context, ShowImageActivity::class.java).apply {
            putExtra(ShowImageActivity.INTENT_EXTRA_KEY_URLS, allImages.toTypedArray())
            putExtra(ShowImageActivity.INTENT_EXTRA_KEY_POSITION, tappedIndex)
        }
        view.context.startActivity(intent)
    }

    fun onClickMediaVideo(view: View, mediaEntity: MediaEntity) {
        val videoUrl = TweetListConverter.videoMediaUrl(mediaEntity)
        val isGif = TweetListConverter.mediaIsGif(mediaEntity)

        val intent = Intent(view.context, ShowVideoActivity::class.java).apply {
            val videoType = if (isGif) ShowVideoActivity.TYPE_GIF else ShowVideoActivity.TYPE_VIDEO
            putExtra(ShowVideoActivity.INTENT_EXTRA_KEY_URL, videoUrl)
            putExtra(ShowVideoActivity.INTENT_EXTRA_KEY_TYPE, videoType)
        }
        view.context.startActivity(intent)
    }

}
