package sugtao4423.lod.ui.adapter.tweet.click.listener

import android.view.View
import android.widget.Button
import android.widget.EditText
import kotlin.math.max
import kotlin.math.min

class RegButtonClickListener(private val regEdit: EditText) : View.OnClickListener {

    override fun onClick(v: View?) {
        val start = regEdit.selectionStart
        val end = regEdit.selectionEnd
        regEdit.text.replace(min(start, end), max(start, end), (v as Button).text)
    }

}
