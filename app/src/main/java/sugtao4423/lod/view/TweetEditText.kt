package sugtao4423.lod.view

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.util.AttributeSet
import android.widget.EditText

class TweetEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle,
) : EditText(context, attrs, defStyleAttr) {

    var prefixLength: Int = 0
        set(value) {
            field = value
            if (selectionStart < value || selectionEnd < value) {
                setSelection(maxOf(selectionStart, value), maxOf(selectionEnd, value))
            }
        }

    init {
        filters = arrayOf(FixedPrefixFilter())
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (prefixLength == 0) return

        val newStart = maxOf(selStart, prefixLength)
        val newEnd = maxOf(selEnd, prefixLength)
        if (newStart != selStart || newEnd != selEnd) {
            setSelection(newStart, newEnd)
        }
    }

    inner class FixedPrefixFilter : InputFilter {
        override fun filter(
            source: CharSequence, start: Int, end: Int,
            dest: Spanned, dstart: Int, dend: Int,
        ): CharSequence? {
            if (dstart >= prefixLength) return null

            val keepEnd = minOf(dend, prefixLength)
            val keptPrefix = dest.subSequence(dstart, keepEnd).toString()

            return if (dend <= prefixLength) {
                keptPrefix
            } else {
                keptPrefix + source.subSequence(start, end)
            }
        }
    }

}
