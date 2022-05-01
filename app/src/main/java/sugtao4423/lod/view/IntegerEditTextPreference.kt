package sugtao4423.lod.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class IntegerEditTextPreference : EditTextPreference {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    override fun getPersistedString(defaultReturnValue: String?): String =
        getPersistedInt(0).toString()

    override fun persistString(value: String?): Boolean {
        val intValue = try {
            Integer.parseInt(value!!)
        } catch (e: NumberFormatException) {
            0
        }
        return persistInt(intValue)
    }
}
