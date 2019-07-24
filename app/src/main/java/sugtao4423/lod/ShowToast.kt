package sugtao4423.lod

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast

class ShowToast(context: Context, text: String, duration: Int) : Toast(context) {

    constructor(context: Context, resId: Int, duration: Int) : this(context, context.getString(resId), duration)

    constructor(context: Context, text: String) : this(context, text, LENGTH_SHORT)

    constructor(context: Context, resId: Int) : this(context, resId, LENGTH_SHORT)

    private val v: View = View.inflate(context, R.layout.custom_toast, null)
    private val message: TextView = v.findViewById(R.id.toastMessage)

    init {
        view = v

        message.text = text
        message.setPadding(3, 2, 3, 2)

        setDuration(duration)
        show()
    }

}