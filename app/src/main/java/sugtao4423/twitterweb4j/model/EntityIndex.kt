package sugtao4423.twitterweb4j.model

import twitter4j.JSONArray
import twitter4j.JSONObject

abstract class EntityIndex : Comparable<EntityIndex>, java.io.Serializable {

    private val start: Int
    private val end: Int

    constructor(start: Int, end: Int) {
        this.start = start
        this.end = end
    }

    constructor(json: JSONObject, key: String = "indices") {
        val indices = json.optJSONArray(key) ?: JSONArray()
        start = indices.optInt(0, -1)
        end = indices.optInt(1, -1)
    }

    override fun compareTo(other: EntityIndex): Int = start - other.start

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
