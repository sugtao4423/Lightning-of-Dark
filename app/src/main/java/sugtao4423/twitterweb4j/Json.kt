package sugtao4423.twitterweb4j

import twitter4j.JSONArray
import twitter4j.JSONException
import twitter4j.JSONObject
import twitter4j.JSONTokener

@JvmInline
value class Json(val raw: Any?) {

    operator fun get(key: String): Json = Json((raw as? JSONObject)?.opt(key))

    operator fun get(index: Int): Json = Json((raw as? JSONArray)?.opt(index))

    val stringOrNull: String? get() = raw as? String
    val intOrNull: Int? get() = (raw as? Number)?.toInt()
    val longOrNull: Long? get() = (raw as? Number)?.toLong()
    val doubleOrNull: Double? get() = (raw as? Number)?.toDouble()
    val boolOrNull: Boolean? get() = raw as? Boolean

    val string: String get() = stringOrNull ?: typeError("String")
    val int: Int get() = intOrNull ?: typeError("Int")
    val long: Long get() = longOrNull ?: typeError("Long")
    val double: Double get() = doubleOrNull ?: typeError("Double")
    val bool: Boolean get() = boolOrNull ?: typeError("Boolean")
    val boolOrFalse: Boolean get() = boolOrNull ?: false

    val isNull: Boolean get() = raw == null || raw === JSONObject.NULL
    fun orNull(): Json? = if (isNull) null else this

    val size: Int
        get() = when (raw) {
            is JSONArray -> raw.length()
            is JSONObject -> raw.length()
            else -> 0
        }

    operator fun iterator(): Iterator<Json> {
        val arr = raw as? JSONArray ?: typeError("JSONArray")
        return object : Iterator<Json> {
            private var i = 0
            override fun hasNext() = i < arr.length()
            override fun next() = Json(arr.opt(i++))
        }
    }

    override fun toString(): String = when (val r = raw) {
        null, JSONObject.NULL -> "null"
        is String -> JSONObject.quote(r)
        else -> r.toString()
    }

    private fun typeError(expected: String): Nothing = throw JSONException(
        "Expected $expected but was ${raw?.javaClass?.simpleName ?: "null"}"
    )

}

fun String.parseJson(): Json = Json(JSONTokener(this).nextValue())
