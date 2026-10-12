package sugtao4423.lod.ui.adapter.tweet.click.listener

import sugtao4423.twitter4j.Status

class TweetDialogClickListeners(
    status: Status,
    allStatuses: List<Status>,
    onClicked: () -> Unit,
) {

    val listItemClickListener = ListItemClickListener(status, allStatuses, onClicked)
    val replyListener = ReplyListener(status, onClicked)
    val retweetListener = RetweetListener(status, onClicked)
    val quoteRTListener = QuoteRTListener(status, onClicked)
    val unOfficialRTListener = UnOfficialRTListener(status, onClicked)
    val favoriteListener = FavoriteListener(status, onClicked)
    val talkListener = TalkListener(status, onClicked)
    val deleteTweetListener = DeleteTweetListener(status, onClicked)

}
