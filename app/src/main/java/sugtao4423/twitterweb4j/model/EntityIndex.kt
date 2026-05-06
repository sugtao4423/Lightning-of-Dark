package sugtao4423.twitterweb4j.model

import sugtao4423.twitterweb4j.Json

open class EntityIndex : Comparable<EntityIndex>, java.io.Serializable {

    private val start: Int
    private val end: Int

    constructor(start: Int, end: Int) {
        this.start = start
        this.end = end
    }

    constructor(json: Json, override: EntityIndex?) {
        start = override?.start ?: json["indices"][0].intOrNull ?: -1
        end = override?.end ?: json["indices"][1].intOrNull ?: -1
    }

    override fun compareTo(other: EntityIndex): Int = start.compareTo(other.start)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EntityIndex) return false
        return start == other.start && end == other.end
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + end
        return result
    }

    fun getStart(): Int = start
    fun getEnd(): Int = end

}
