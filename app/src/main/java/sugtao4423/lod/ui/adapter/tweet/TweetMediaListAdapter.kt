package sugtao4423.lod.ui.adapter.tweet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.databinding.ListItemTweetMediaImageBinding
import sugtao4423.lod.databinding.ListItemTweetMediaVideoBinding
import sugtao4423.lod.ui.adapter.converter.TweetViewDataConverter
import twitter4j.MediaEntity

private object MediaEntityDiffCallback : DiffUtil.ItemCallback<MediaEntity>() {

    override fun areItemsTheSame(oldItem: MediaEntity, newItem: MediaEntity): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MediaEntity, newItem: MediaEntity): Boolean =
        areItemsTheSame(oldItem, newItem)
}

class TweetMediaListAdapter(private val tweetListViewModel: TweetListViewModel) :
    ListAdapter<MediaEntity, RecyclerView.ViewHolder>(MediaEntityDiffCallback) {

    companion object {
        const val VIEW_TYPE_IMAGE = 0
        const val VIEW_TYPE_VIDEO = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                ImageViewHolder(ListItemTweetMediaImageBinding.inflate(inflater, parent, false))
            }
            VIEW_TYPE_VIDEO -> {
                VideoViewHolder(ListItemTweetMediaVideoBinding.inflate(inflater, parent, false))
            }
            else -> throw UnsupportedOperationException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (getItemViewType(position)) {
            VIEW_TYPE_IMAGE -> (holder as ImageViewHolder).bind(item, position)
            VIEW_TYPE_VIDEO -> (holder as VideoViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (TweetViewDataConverter.mediaIsVideoOrGif(item)) {
            return VIEW_TYPE_VIDEO
        }
        return VIEW_TYPE_IMAGE
    }

    inner class ImageViewHolder(private val binding: ListItemTweetMediaImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaEntity: MediaEntity, position: Int) {
            binding.also {
                it.viewModel = tweetListViewModel
                it.thumbnailUrl = TweetViewDataConverter.mediaThumbnailUrl(mediaEntity)
                it.allImages = TweetViewDataConverter.allImageUrls(currentList)
                it.tappedIndex = position
                it.executePendingBindings()
            }
        }
    }

    inner class VideoViewHolder(private val binding: ListItemTweetMediaVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaEntity: MediaEntity) {
            binding.also {
                it.viewModel = tweetListViewModel
                it.thumbnailUrl = TweetViewDataConverter.mediaThumbnailUrl(mediaEntity)
                it.videoUrl = TweetViewDataConverter.videoMediaUrl(mediaEntity)
                it.isGif = TweetViewDataConverter.mediaIsGif(mediaEntity)
                it.executePendingBindings()
            }
        }
    }

}
