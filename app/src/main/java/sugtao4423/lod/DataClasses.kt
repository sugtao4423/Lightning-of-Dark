package sugtao4423.lod

import android.content.Context
import androidx.preference.PreferenceManager
import sugtao4423.lod.tweetlistview.TweetListAdapter
import kotlin.math.ln

data class TwitterList(
        val adapter: TweetListAdapter,
        var isAlreadyLoad: Boolean,
        val listName: String,
        val listId: Long,
        val isAppStartLoad: Boolean
)

class Options(context: Context) {
    private val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val isOpenBrowser = pref.getBoolean(Keys.MENU_OPEN_BROWSER, false)
    val isRegex = pref.getBoolean(Keys.MENU_REGEX, false)
    val isMillisecond = pref.getBoolean(Keys.MENU_MILLISECOND, false)
    val nowplayingFormat = pref.getString(Keys.NOWPLAYING_FORMAT, "") ?: ""
    val isImageOrientationSensor = pref.getBoolean(Keys.IS_IMAGE_ORIENTATION_SENSOR, false)
    val isVideoOrientationSensor = pref.getBoolean(Keys.IS_VIDEO_ORIENTATION_SENSOR, false)
    val userNameFontSize = (pref.getString(Keys.USER_NAME_FONT_SIZE, "13") ?: "13").toFloat()
    val contentFontSize = (pref.getString(Keys.CONTENT_FONT_SIZE, "13") ?: "13").toFloat()
    val dateFontSize = (pref.getString(Keys.DATE_FONT_SIZE, "11") ?: "11").toFloat()
}

class Level(context: Context) {

    private val pref = PreferenceManager.getDefaultSharedPreferences(context)
    private var experience = pref.getInt(Keys.EXPERIENCE, 0)

    fun getLevel(): Int {
        var level = 0
        var exp = 0
        while (experience >= exp) {
            val i = level * 100
            exp += ln(i.toDouble()).toInt() * i
            level++
        }
        return level - 1
    }

    fun getNextExp(): Int {
        val level = getLevel() + 1
        var exp = 0
        for (i in 0 until level) {
            val j = i * 100
            exp += ln(j.toDouble()).toInt() * j
        }
        return exp - experience
    }

    fun getTotalExp(): Int {
        return experience
    }

    /**
     * @param exp
     * @return is level up
     */
    fun addExp(exp: Int): Boolean {
        val oldLevel = getLevel()
        experience += exp
        pref.edit().putInt(Keys.EXPERIENCE, experience).apply()
        return oldLevel != getLevel()
    }

    /**
     * @return (0 - 150 random + level) exp
     */
    fun getRandomExp(): Int {
        return (Math.random() * 150.0).toInt() + getLevel()
    }

}
