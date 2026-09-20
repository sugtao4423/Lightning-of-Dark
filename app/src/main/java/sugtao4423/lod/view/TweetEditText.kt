package sugtao4423.lod.view

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.twitter.twittertext.Extractor
import sugtao4423.lod.R

class TweetEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val extractor = Extractor()
    private val entityColor = ContextCompat.getColor(context, R.color.twitterBrand)

    var prefixLength: Int = 0
        set(value) {
            field = value
            if (selectionStart < value || selectionEnd < value) {
                setSelection(maxOf(selectionStart, value), maxOf(selectionEnd, value))
            }
        }

    init {
        doAfterTextChanged {
            applyEntityColor(it ?: return@doAfterTextChanged)
        }
        filters = arrayOf(FixedPrefixFilter())
    }

    private fun applyEntityColor(editable: Editable) {
        editable.getSpans(0, editable.length, ForegroundColorSpan::class.java).forEach {
            editable.removeSpan(it)
        }
        extractor.extractEntitiesWithIndices(editable.toString()).forEach {
            editable.setSpan(ForegroundColorSpan(entityColor), it.start, it.end, 0)
        }
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
