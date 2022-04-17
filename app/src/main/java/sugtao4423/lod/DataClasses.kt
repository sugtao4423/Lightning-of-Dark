package sugtao4423.lod

import sugtao4423.lod.tweetlistview.TweetListAdapter

data class TwitterList(
    val adapter: TweetListAdapter,
    var isAlreadyLoad: Boolean,
    val listName: String,
    val listId: Long,
    val isAppStartLoad: Boolean
)
