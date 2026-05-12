package sugtao4423.lod.utils

import sugtao4423.twitter4j.Status

fun Status.toStatusUrl(): String = "https://twitter.com/${user.screenName}/status/${id}"
