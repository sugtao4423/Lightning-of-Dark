package sugtao4423.lod

import android.content.Context
import sugtao4423.lod.tweetlistview.TweetListAdapter
import kotlin.math.ln

data class TwitterList(
        val adapter: TweetListAdapter,
        var isAlreadyLoad: Boolean,
        val listName: String,
        val listId: Long,
        val isAppStartLoad: Boolean
)

class Level(context: Context) {

    private val prefRepo = (context.applicationContext as App).prefRepository

    fun getLevel(): Int {
        var level = 0
        var exp = 0
        while (prefRepo.experience >= exp) {
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
        return exp - prefRepo.experience
    }

    fun getTotalExp(): Int = prefRepo.experience

    /**
     * @param exp
     * @return is level up
     */
    fun addExp(exp: Int): Boolean {
        val oldLevel = getLevel()
        val newExp = prefRepo.experience + exp
        prefRepo.experience = newExp
        return oldLevel != getLevel()
    }

    /**
     * @return (0 - 150 random + level) exp
     */
    fun getRandomExp(): Int = (Math.random() * 150.0).toInt() + getLevel()

}
