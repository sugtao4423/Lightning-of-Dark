package sugtao4423.twitterweb4j.body

import org.json.JSONException
import org.json.JSONObject

abstract class BaseBody(requestUrl: String) {

    protected val queryId = requestUrl.split("/").takeLast(2).first()

    protected open val features: Map<String, Any>? = null

    protected abstract val variables: Map<String, Any>

    @Throws(JSONException::class)
    protected fun buildJsonString(variables: Map<String, Any>): String {
        return JSONObject().apply {
            put("queryId", queryId)
            features?.let {
                put("features", JSONObject(it))
            }
            put("variables", JSONObject(variables))
        }.toString()
    }

}
