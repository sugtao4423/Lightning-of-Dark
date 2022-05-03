package sugtao4423.lod.ui.adapter.tweet.click

import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ui.adapter.converter.TweetViewDataConverter
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter

class OnTweetItemClicked(private val tweetListAdapter: TweetListAdapter) {

    private val context = tweetListAdapter.context
    private val app = context.applicationContext as App

    private val tweetItemDialog by lazy { TweetItemDialog(context, app.fontAwesomeTypeface) }

    fun onItemClicked(position: Int) {
        val status = tweetListAdapter.data[position]
        val dialogList = arrayListOf<String>()

        if (app.prefRepository.isRegex) {
            dialogList.add(context.getString(R.string.extract_with_regex))
        }
        if (app.prefRepository.isOpenBrowser) {
            dialogList.add(context.getString(R.string.open_in_browser))
        }

        val users = (
                listOf(status.user.screenName) + status.userMentionEntities.map { it.screenName }
                ).distinct()
        dialogList.addAll(users.map { "@${it}" })

        dialogList.addAll(status.urlEntities.map { it.expandedURL })

        val mediaUrls = status.mediaEntities.map {
            if (TweetViewDataConverter.mediaIsVideoOrGif(it)) {
                TweetViewDataConverter.videoMediaUrl(it)
            } else {
                it.mediaURLHttps
            }
        }
        dialogList.addAll(mediaUrls)

        val original = TweetViewDataConverter.originalStatus(status)!!
        tweetItemDialog.show(context, original, tweetListAdapter.data, dialogList)
    }
}
