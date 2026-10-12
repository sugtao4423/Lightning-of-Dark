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

class TweetListAdapter(context: Context) :
    UpdatableListAdapter<Status, TweetListAdapter.ViewHolder>() {

    private val tweetListViewModel = TweetListViewModel(context.applicationContext as App)
    var hideImages = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemTweetBinding.inflate(inflater, parent, false)
        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            OnTweetItemClicked.onItemClicked(it, data, pos)
        }
        holder.itemView.setOnLongClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener false
            OnTweetItemLongClicked.onItemLongClicked(it, getItem(pos))
            true
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (itemCount <= position) return
        holder.bind(getItem(position))
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
