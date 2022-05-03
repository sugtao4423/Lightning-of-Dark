package sugtao4423.lod.ui.adapter.tweet

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.App
import sugtao4423.lod.databinding.ListItemTweetBinding
import sugtao4423.lod.ui.adapter.tweet.click.OnTweetItemClicked
import sugtao4423.lod.ui.adapter.tweet.click.OnTweetItemLongClicked
import twitter4j.Status

class TweetListAdapter(val context: Context) : RecyclerView.Adapter<TweetListAdapter.ViewHolder>() {

    private val tweetListViewModel = TweetListViewModel(context.applicationContext as App)
    private val onTweetItemClicked = OnTweetItemClicked(this)
    private val onTweetItemLongClicked = OnTweetItemLongClicked(this)
    val data = arrayListOf<Status>()
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
            onTweetItemClicked.onItemClicked(position)
        }
        holder.itemView.setOnLongClickListener {
            onTweetItemLongClicked.onItemLongClicked(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun add(status: Status) {
        data.add(status)
        notifyItemInserted(data.size - 1)
    }

    fun addAll(statuses: List<Status>) {
        val pos = data.size
        data.addAll(statuses)
        notifyItemRangeInserted(pos, statuses.size)
    }

    fun insertTop(item: List<Status>) {
        data.addAll(0, item)
        notifyItemRangeInserted(0, item.size)
    }

    fun clear() {
        val size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size)
    }

    inner class ViewHolder(private val binding: ListItemTweetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(status: Status) {
            binding.also {
                it.viewModel = tweetListViewModel
                it.status = status
                it.hideImages = hideImages
                it.executePendingBindings()
            }
        }
    }

}
