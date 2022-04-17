package sugtao4423.lod.model

import kotlin.math.ln

class LevelRepository(private val prefRepository: PrefRepository) {

    fun getLevel(): Int {
        var level = 0
        var exp = 0
        while (prefRepository.experience >= exp) {
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
        return exp - prefRepository.experience
    }

    fun getTotalExp(): Int = prefRepository.experience

    /**
     * @param exp
     * @return is level up
     */
    fun addExp(exp: Int): Boolean {
        val oldLevel = getLevel()
        val newExp = prefRepository.experience + exp
        prefRepository.experience = newExp
        return oldLevel != getLevel()
    }

    /**
     * @return (0 - 150 random + level) exp
     */
    fun getRandomExp(): Int = (Math.random() * 150.0).toInt() + getLevel()

}
