package sugtao4423.twitterweb4j.body

import okhttp3.HttpUrl
import org.json.JSONObject

abstract class BaseBody(requestUrl: HttpUrl) {

    protected val queryId = requestUrl.encodedPathSegments.takeLast(2).first()

    protected open val features: Map<String, Any>? = null

    protected open val variables: Map<String, Any>? = null

    protected fun buildJsonString(variables: Map<String, Any>): String {
        return JSONObject().also { json ->
            json.put("queryId", queryId)
            features?.let { json.put("features", JSONObject(it)) }
            json.put("variables", JSONObject(variables))
        }.toString()
    }

}
