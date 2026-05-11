package sugtao4423.twitter4j

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class TwitterException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    constructor(cause: Exception) : this(cause.message, cause)

    val isCausedByNetworkIssue = cause is IOException
    val isCausedByJsonIssue = cause is JSONException

    var errorMessage: String? = null
        private set
    var errorCode: Int? = null
        private set

    init {
        runCatching { decode(message) }
    }

    private fun decode(message: String?) {
        if (message == null || !message.startsWith("{")) return

        val json = JSONObject(message)
        if (json.isNull("errors")) return

        val error = json.getJSONArray("errors").getJSONObject(0)
        errorMessage = error.getString("message")
        val codeStr = error.optString("code", "-1")
        if (codeStr != "-1") {
            errorCode = codeStr.toInt()
        }
    }

}
