package sugtao4423.twitterweb4j.body

import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

abstract class BaseBody(url: HttpUrl) {

    protected val queryId = url.encodedPathSegments.takeLast(2).first()

    protected open val features: Map<String, Any>? = null

    protected fun buildJsonBody(variables: Map<String, Any>): RequestBody =
        JSONObject().also { json ->
            json.put("queryId", queryId)
            features?.let { json.put("features", JSONObject(it)) }
            json.put("variables", JSONObject(variables))
        }.toString().toRequestBody("application/json".toMediaType())

}
