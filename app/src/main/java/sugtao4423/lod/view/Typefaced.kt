package sugtao4423.lod.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import sugtao4423.lod.App

private fun Context.typeface() = (applicationContext as App).fontAwesomeTypeface

@Suppress("DeprecatedCallableAddReplaceWith")
class TypefacedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        super.setTypeface(context.typeface())
    }

    @Deprecated("Typeface is fixed", level = DeprecationLevel.ERROR)
    override fun setTypeface(tf: Typeface?) = super.setTypeface(tf)

    @Deprecated("Typeface is fixed", level = DeprecationLevel.ERROR)
    override fun setTypeface(tf: Typeface?, style: Int) = super.setTypeface(tf, style)
}

@Suppress("DeprecatedCallableAddReplaceWith")
class TypefacedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {
    init {
        super.setTypeface(context.typeface())
    }

    @Deprecated("Typeface is fixed", level = DeprecationLevel.ERROR)
    override fun setTypeface(tf: Typeface?) = super.setTypeface(tf)

    @Deprecated("Typeface is fixed", level = DeprecationLevel.ERROR)
    override fun setTypeface(tf: Typeface?, style: Int) = super.setTypeface(tf, style)
}
