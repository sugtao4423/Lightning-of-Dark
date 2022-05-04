package sugtao4423.lod.ui.adapter.tweet.click.listener

import android.content.Context
import twitter4j.Status

class TweetDialogClickListeners(
    context: Context,
    status: Status,
    allStatusData: ArrayList<Status>,
    onClicked: () -> Unit,
) {

    val listItemClickListener = ListItemClickListener(status, allStatusData, onClicked)
    val replyListener = ReplyListener(status, context, onClicked)
    val retweetListener = RetweetListener(status, context, onClicked)
    val quoteRTListener = QuoteRTListener(status, context, onClicked)
    val unOfficialRTListener = UnOfficialRTListener(status, context, onClicked)
    val favoriteListener = FavoriteListener(status, context, onClicked)
    val talkListener = TalkListener(status, context, onClicked)
    val deleteTweetListener = DeleteTweetListener(status, context, onClicked)

}
