package sugtao4423.lod.utils

import twitter4j.Status

fun Status.toStatusUrl(): String = "https://twitter.com/${user.screenName}/status/${id}"
