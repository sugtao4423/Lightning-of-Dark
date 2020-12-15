package sugtao4423.lod

import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

class ShowToast(context: Context, resId: Int, vararg formatArgs: Any?) : Toast(context) {

    constructor(context: Context, resId: Int) : this(context, resId, null)

    private val errorMessageRes = arrayOf(
            R.string.error_auto_load_tl_interval,
            R.string.error_create_list,
            R.string.error_favorite,
            R.string.error_follow2list,
            R.string.error_get_access_token,
            R.string.error_get_favorite,
            R.string.error_get_follow,
            R.string.error_get_follower,
            R.string.error_get_image,
            R.string.error_get_list,
            R.string.error_get_mention,
            R.string.error_get_original_image,
            R.string.error_get_request_token,
            R.string.error_get_status,
            R.string.error_get_talk_list,
            R.string.error_get_timeline,
            R.string.error_get_user_detail,
            R.string.error_get_user_icon,
            R.string.error_get_video,
            R.string.error_post_delete,
            R.string.error_retweet,
            R.string.error_select_picture,
            R.string.error_tweet,
            R.string.error_unfavorite,
            R.string.error_unretweet,
            R.string.invalid_pattern,
            R.string.param_account_already_exists
    )

    private val showLongRes = arrayOf(
            R.string.error_auto_load_tl_interval,
            R.string.error_get_image,
            R.string.invalid_pattern,
            R.string.param_account_already_exists,
            R.string.param_level_up,
            R.string.param_regex_result_count,
            R.string.param_saved,
            R.string.param_saved_original
    )

    private val v: View = View.inflate(context, R.layout.custom_toast, null)
    private val message: TextView = v.findViewById(R.id.toastMessage)

    init {
        if (errorMessageRes.contains(resId)) {
            v.setBackgroundResource(R.drawable.toast_error_bg)

            val appSize = Point()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(appSize)
            setGravity(Gravity.TOP, 0, appSize.y / 6)
        }
        duration = if (showLongRes.contains(resId)) {
            LENGTH_LONG
        } else {
            LENGTH_SHORT
        }
        view = v

        message.text = context.getString(resId, *formatArgs)
        message.setPadding(3, 2, 3, 2)

        show()
    }

}