package sugtao4423.lod.ui.adapter.tweet

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.App
import sugtao4423.lod.databinding.ListItemTweetBinding
import sugtao4423.lod.ui.adapter.UpdatableListAdapter
import sugtao4423.lod.ui.adapter.converter.TweetListConverter
import sugtao4423.lod.ui.adapter.tweet.click.OnTweetItemClicked
import sugtao4423.lod.ui.adapter.tweet.click.OnTweetItemLongClicked
import sugtao4423.lod.ui.loadUrl
import sugtao4423.twitter4j.Status

class TweetListAdapter(val context: Context) :
    UpdatableListAdapter<Status, TweetListAdapter.ViewHolder>() {

    private val tweetListViewModel = TweetListViewModel(context.applicationContext as App)
    private val onTweetItemClicked = OnTweetItemClicked(this)
    private val onTweetItemLongClicked = OnTweetItemLongClicked(this)
    var hideImages = false

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemTweetBinding.inflate(inflater, viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.size <= position) {
            return
        }
        holder.bind(data[position])

        holder.itemView.setOnClickListener {
            onTweetItemClicked.onItemClicked(holder.layoutPosition)
        }
        holder.itemView.setOnLongClickListener {
            onTweetItemLongClicked.onItemLongClicked(holder.layoutPosition)
            true
        }
    }

    inner class ViewHolder(private val binding: ListItemTweetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(status: Status) = binding.apply {
            val vm = tweetListViewModel

            userIcon.loadUrl(TweetListConverter.userIconUrl(status))
            userIcon.setOnClickListener { vm.onClickUserIcon(it, status) }

            userName.text = TweetListConverter.userNameAndScreenName(status)
            userName.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.nameTextSize)

            val isShowProtected = TweetListConverter.isShowProtected(status)
            protectIcon.visibility = if (isShowProtected) View.VISIBLE else View.GONE
            protectIcon.typeface = vm.fontAwesomeTypeface
            protectIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.protectIconSize)

            tweetText.text = TweetListConverter.text(status)
            tweetText.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.textSize)

            tweetDate.text = TweetListConverter.date(status, vm.isShowMilliSeconds)
            tweetDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.dateTextSize)

            val isShowRetweet = TweetListConverter.isShowRetweetUser(status)
            retweetUserIcon.visibility = if (isShowRetweet) View.VISIBLE else View.GONE
            retweetUserName.visibility = if (isShowRetweet) View.VISIBLE else View.GONE
            if (isShowRetweet) {
                retweetUserIcon.loadUrl(TweetListConverter.retweetedUserIconUrl(status))
                retweetUserName.text = TweetListConverter.retweetedUserScreenName(status)
                retweetUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.dateTextSize)
            }

            val isShowMedias = !hideImages && TweetListConverter.isShowMediaList(status)
            mediaRecyclerView.visibility = if (isShowMedias) View.VISIBLE else View.GONE
            if (isShowMedias) {
                mediaRecyclerView.adapter = vm.tweetMediaAdapter(status)
            }
        }
    }

}
