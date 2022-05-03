package sugtao4423.lod.ui.adapter

import android.content.Intent
import android.view.View
import sugtao4423.lod.App
import sugtao4423.lod.ui.adapter.converter.TweetViewDataConverter
import sugtao4423.lod.ui.userpage.UserPageActivity
import twitter4j.Status

class TweetViewModel(app: App) {

    val fontAwesomeTypeface = app.fontAwesomeTypeface
    val protectIconSize = app.prefRepository.userNameFontSize - 3
    val nameTextSize = app.prefRepository.userNameFontSize
    val textSize = app.prefRepository.contentFontSize
    val dateTextSize = app.prefRepository.dateFontSize
    val isShowMilliSeconds = app.prefRepository.isMillisecond

    fun onClickUserIcon(view: View, status: Status) {
        val intent = Intent(view.context, UserPageActivity::class.java).apply {
            putExtra(
                UserPageActivity.INTENT_EXTRA_KEY_USER_OBJECT,
                TweetViewDataConverter.originalStatus(status)!!.user
            )
        }
        view.context.startActivity(intent)
    }

}
