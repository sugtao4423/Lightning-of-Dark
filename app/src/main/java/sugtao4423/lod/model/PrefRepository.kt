package sugtao4423.lod.model

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class PrefRepository(context: Context) {

    private val pref = PreferenceManager.getDefaultSharedPreferences(context)

    var screenName: String
        get() = pref.getString("screenName", "") ?: ""
        set(value) = pref.edit { putString("screenName", value) }

    var regularExpression: String
        get() = pref.getString("regularExpression", "") ?: ""
        set(value) = pref.edit { putString("regularExpression", value) }

    var experience: Int
        get() = pref.getInt("experience", 0)
        set(value) = pref.edit { putInt("experience", value) }

    var autoLoadTLInterval: Int
        get() = pref.getInt("autoLoadTLInterval", 0)
        set(value) = pref.edit { putInt("autoLoadTLInterval", value) }

    val isOpenBrowser: Boolean
        get() = pref.getBoolean("menu_openBrowser", false)
    val isRegex: Boolean
        get() = pref.getBoolean("menu_regex", false)
    val isMillisecond: Boolean
        get() = pref.getBoolean("menu_millisecond", false)
    val nowPlayingFormat: String
        get() = pref.getString("nowPlayingFormat", "") ?: ""
    val isImageOrientationSensor: Boolean
        get() = pref.getBoolean("isImageOrientationSensor", false)
    val isVideoOrientationSensor: Boolean
        get() = pref.getBoolean("isVideoOrientationSensor", false)
    val userNameFontSize: Float
        get() = (pref.getString("userNameFontSize", "13") ?: "13").toFloat()
    val contentFontSize: Float
        get() = (pref.getString("contentFontSize", "13") ?: "13").toFloat()
    val dateFontSize: Float
        get() = (pref.getString("dateFontSize", "11") ?: "11").toFloat()

}
