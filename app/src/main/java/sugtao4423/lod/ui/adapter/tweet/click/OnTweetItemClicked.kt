package sugtao4423.lod.ui.adapter.tweet.click

import android.view.View
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.utils.getHighestBitrateUrl
import sugtao4423.lod.utils.original
import sugtao4423.lod.utils.toStatusUrl
import sugtao4423.twitter4j.MediaEntity
import sugtao4423.twitter4j.Status

object OnTweetItemClicked {

    fun onItemClicked(view: View, allStatuses: List<Status>, position: Int) {
        val context = view.context

        val status = allStatuses[position]
        val originalStatus = status.original()

        val dialogList = arrayListOf<String>()

        val prefRepository = (context.applicationContext as App).prefRepository
        if (prefRepository.isRegex) {
            dialogList.add(context.getString(R.string.extract_with_regex))
        }
        if (prefRepository.isOpenBrowser) {
            dialogList.add(context.getString(R.string.open_in_browser))
        }

        val screenNames =
            setOf(status.user.screenName) + status.userMentionEntities.map { it.screenName }.toSet()
        screenNames.forEach {
            dialogList.add("@${it}")
        }

        originalStatus.urlEntities.forEach {
            dialogList.add(it.expandedUrl ?: it.url)
        }

        originalStatus.mediaEntities.forEach {
            val url = when (it) {
                is MediaEntity.Image -> it.mediaUrl
                is MediaEntity.Gif, is MediaEntity.Video -> it.videoInfo.getHighestBitrateUrl()
            }
            dialogList.add(url)
        }

        originalStatus.quotedStatus?.let {
            dialogList.add(it.toStatusUrl())
        }

        TweetItemDialog(context).show(originalStatus, allStatuses, dialogList)
    }

}
