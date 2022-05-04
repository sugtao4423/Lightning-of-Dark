package sugtao4423.lod.ui.adapter.user

import android.content.Intent
import android.view.View
import sugtao4423.lod.App
import sugtao4423.lod.ui.userpage.UserPageActivity
import twitter4j.User

class UserListViewModel(app: App) {

    val fontAwesomeTypeface = app.fontAwesomeTypeface
    val protectIconSize = app.prefRepository.userNameFontSize - 3
    val nameTextSize = app.prefRepository.userNameFontSize
    val textSize = app.prefRepository.contentFontSize
    val dateTextSize = app.prefRepository.dateFontSize

    fun onClickUser(view: View, user: User) {
        val intent = Intent(view.context, UserPageActivity::class.java).apply {
            putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_OBJECT, user)
        }
        view.context.startActivity(intent)
    }

}
