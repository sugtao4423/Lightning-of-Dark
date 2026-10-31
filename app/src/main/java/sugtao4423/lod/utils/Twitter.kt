package sugtao4423.lod.utils

import sugtao4423.twitter4j.Status
import sugtao4423.twitter4j.VideoInfo

fun Status.original(): Status = if (isRetweet) retweetedStatus!! else this
fun Status.toStatusUrl(): String = "https://twitter.com/${user.screenName}/status/${id}"

fun VideoInfo.getHighestBitrateUrl(): String = variants.maxBy { it.bitrate }.url
