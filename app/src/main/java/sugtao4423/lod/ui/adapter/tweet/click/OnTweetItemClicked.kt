package sugtao4423.lod.ui.adapter.tweet.click

import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ui.adapter.converter.TweetListConverter
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.utils.toStatusUrl

class OnTweetItemClicked(private val tweetListAdapter: TweetListAdapter) {

    private val context = tweetListAdapter.context
    private val app = context.applicationContext as App

    private val tweetItemDialog by lazy { TweetItemDialog(context, app.fontAwesomeTypeface) }

    fun onItemClicked(position: Int) {
        val status = tweetListAdapter.data[position]
        val originalStatus = TweetListConverter.originalStatus(status)!!

        val dialogList = arrayListOf<String>()

        if (app.prefRepository.isRegex) {
            dialogList.add(context.getString(R.string.extract_with_regex))
        }
        if (app.prefRepository.isOpenBrowser) {
            dialogList.add(context.getString(R.string.open_in_browser))
        }

        val screenNames =
            setOf(status.user.screenName) + status.userMentionEntities.map { it.screenName }.toSet()
        screenNames.forEach {
            dialogList.add("@${it}")
        }

        originalStatus.urlEntities.forEach {
            dialogList.add(it.expandedURL)
        }

        originalStatus.mediaEntities.forEach {
            val url = if (TweetListConverter.mediaIsVideoOrGif(it)) {
                TweetListConverter.videoMediaUrl(it)
            } else {
                it.mediaURLHttps
            }
            dialogList.add(url)
        }

        originalStatus.quotedStatus?.let {
            dialogList.add(it.toStatusUrl())
        }

        tweetItemDialog.show(context, originalStatus, tweetListAdapter.data, dialogList)
    }
}
