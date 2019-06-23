package sugtao4423.lod

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.loopj.android.image.SmartImageView

class ShowToast(context: Context, text: String, duration: Int, toastType: Type) : Toast(context) {

    constructor(context: Context, resId: Int, duration: Int, toastType: Type) : this(context, context.getString(resId), duration, toastType)

    constructor(context: Context, text: String, duration: Int) : this(context, text, duration, Type.NORMAL)

    constructor(context: Context, resId: Int, duration: Int) : this(context, resId, duration, Type.NORMAL)

    constructor(context: Context, text: String) : this(context, text, LENGTH_SHORT, Type.NORMAL)

    constructor(context: Context, resId: Int) : this(context, resId, LENGTH_SHORT, Type.NORMAL)

    enum class Type {
        NORMAL

    }

    private val v: View = View.inflate(context, R.layout.custom_toast, null)
    private val mainMessage: TextView = v.findViewById(R.id.toast_main_message)
    private val tweet: TextView = v.findViewById(R.id.toast_tweet)
    private val icon: SmartImageView = v.findViewById(R.id.toast_icon)

    init {
        view = v

        when (toastType) {
            Type.NORMAL -> normalToast(text, duration)
        }
    }

    private fun normalToast(text: String, duration: Int) {
        mainMessage.text = text
        mainMessage.setTextColor(Color.WHITE)
        mainMessage.setPadding(3, 2, 3, 2)

        tweet.visibility = View.GONE
        icon.visibility = View.GONE

        setDuration(duration)
        show()
    }

}