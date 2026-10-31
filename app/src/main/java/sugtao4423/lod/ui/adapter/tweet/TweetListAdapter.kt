package sugtao4423.lod.ui.adapter.tweet

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.App
import sugtao4423.lod.databinding.ListItemTweetBinding
import sugtao4423.lod.ui.adapter.UpdatableListAdapter
import sugtao4423.lod.ui.adapter.tweet.click.OnTweetItemClicked
import sugtao4423.lod.ui.adapter.tweet.click.OnTweetItemLongClicked
import sugtao4423.lod.ui.loadUrl
import sugtao4423.lod.utils.original
import sugtao4423.twitter4j.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemTweetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(status: Status) = binding.apply {
            val vm = tweetListViewModel
            val original = status.original()

            userIcon.loadUrl(original.user.profileImage?.biggerUrl)
            userIcon.setOnClickListener { vm.onClickUserIcon(it, status) }

            userName.text = original.user.let { "${it.name} - @${it.screenName}" }
            userName.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.nameTextSize)

            protectIcon.isVisible = original.user.isProtected
            protectIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.protectIconSize)

            tweetText.text = original.text
            tweetText.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.textSize)

            tweetDate.text = statusDates(status)
            tweetDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.dateTextSize)

            val isShowRetweet = status.isRetweet
            retweetUserIcon.isVisible = isShowRetweet
            retweetUserName.isVisible = isShowRetweet
            if (isShowRetweet) {
                retweetUserIcon.loadUrl(status.user.profileImage?.biggerUrl)
                retweetUserName.text = "@${status.user.screenName}"
                retweetUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, vm.dateTextSize)
            }

            val isShowMedias = !hideImages && original.mediaEntities.isNotEmpty()
            mediaRecyclerView.isVisible = isShowMedias
            if (isShowMedias) {
                mediaRecyclerView.adapter = vm.tweetMediaAdapter(status)
            }
        }
    }

    private fun statusDates(status: Status): String {
        val isShowMilliSec = tweetListViewModel.isShowMilliSeconds
        val sdfPattern = "yyyy/MM/dd HH:mm:ss" + (if (isShowMilliSec) ".SSS" else "")
        val date = SimpleDateFormat(sdfPattern, Locale.getDefault())
            .format(Date((status.original().id shr 22) + 1288834974657L))
        return when {
            status.isRetweet -> "$date  Retweeted by "
            status.source == null -> date
            else -> {
                val via = status.source.replace(Regex("<.+?>"), "")
                "$date  via $via"
            }
        }
    }

}
