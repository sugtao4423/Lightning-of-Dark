package sugtao4423.lod.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter

class TweetListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    val linearLayoutManager: LinearLayoutManager

    init {
        isVerticalScrollBarEnabled = true
        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        addItemDecoration(AlternatelyColor(context))
        linearLayoutManager = LinearLayoutManager(context)
        layoutManager = linearLayoutManager
    }

    inner class AlternatelyColor(context: Context) : ItemDecoration() {

        private val app = context.applicationContext as App

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            super.getItemOffsets(outRect, view, parent, state)
            val pos = parent.getChildAdapterPosition(view)
            if (pos < 0) {
                return
            }
            val adapter = parent.adapter
            val backgroundResource = if (adapter is TweetListAdapter) {
                val item = adapter.data[pos]
                when {
                    item.isRetweetedByMe ->
                        R.drawable.selector_list_retweeted_by_me_bg
                    item.isRetweet ->
                        R.drawable.selector_list_retweet_bg
                    item.user.screenName == app.account.screenName ->
                        R.drawable.selector_list_same_my_screenname_bg
                    app.mentionPattern.matcher(item.text).find() ->
                        R.drawable.selector_list_mention_bg
                    else -> alternatelyResource(pos)
                }
            } else {
                alternatelyResource(pos)
            }
            view.setBackgroundResource(backgroundResource)
        }

        private fun alternatelyResource(pos: Int): Int =
            if (pos % 2 == 0) R.drawable.selector_list_position0_bg else R.drawable.selector_list_position1_bg

    }

}
