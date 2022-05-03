package sugtao4423.lod.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.StatusClickListener
import sugtao4423.lod.databinding.ListItemTweetBinding
import sugtao4423.lod.ui.showimage.ShowImageActivity
import sugtao4423.lod.ui.showvideo.ShowVideoActivity
import sugtao4423.lod.utils.Utils
import twitter4j.Status

class TweetListAdapter(val context: Context) : RecyclerView.Adapter<TweetListAdapter.ViewHolder>() {

    private val app = context.applicationContext as App
    private val tweetViewModel = TweetViewModel(app)
    private val statusClickListener = StatusClickListener()
    val data = arrayListOf<Status>()
    var hideImages = false

    interface OnItemClickListener {
        fun onItemClicked(tweetListAdapter: TweetListAdapter, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(tweetListAdapter: TweetListAdapter, position: Int): Boolean
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemTweetBinding.inflate(inflater, viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.size <= position) {
            return
        }
        val item = data[position]
        val origStatus = if (item.isRetweet) item.retweetedStatus else item

        holder.bind(tweetViewModel, item)

        holder.itemView.setOnClickListener {
            statusClickListener.onItemClicked(this, holder.layoutPosition)
        }
        holder.itemView.setOnLongClickListener {
            statusClickListener.onItemLongClicked(this, holder.layoutPosition)
        }

        val mentitys = origStatus.mediaEntities
        if (mentitys.isNotEmpty() && !hideImages) {
            holder.tweetImagesScroll.visibility = View.VISIBLE
            holder.tweetImagesLayout.removeAllViews()
            mentitys.mapIndexed { index, media ->
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 200)
                if (holder.tweetImagesLayout.childCount != 0) {
                    params.setMargins(8, 0, 0, 0)
                }
                val child = ImageView(context).apply {
                    layoutParams = params
                    maxHeight = 200
                    adjustViewBounds = true
                }

                if (Utils.isVideoOrGif(media)) {
                    val centerParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 200, Gravity.CENTER)
                    val play = ImageView(context).apply {
                        layoutParams = centerParams
                        maxHeight = 200
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageResource(R.drawable.icon_video_play)
                    }

                    val fl = FrameLayout(context)
                    fl.addView(child)
                    fl.addView(play)
                    holder.tweetImagesLayout.addView(fl)

                    val videoUrl = Utils.getHiBitrateVideoUrl(mentitys)
                    Glide.with(app.applicationContext).load(media.mediaURLHttps + ":small").placeholder(R.drawable.icon_loading).into(child)
                    child.setOnClickListener {
                        val intent = Intent(context, ShowVideoActivity::class.java).apply {
                            val videoType = if (Utils.isGif(media)) ShowVideoActivity.TYPE_GIF else ShowVideoActivity.TYPE_VIDEO
                            putExtra(ShowVideoActivity.INTENT_EXTRA_KEY_URL, videoUrl)
                            putExtra(ShowVideoActivity.INTENT_EXTRA_KEY_TYPE, videoType)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    holder.tweetImagesLayout.addView(child)
                    Glide.with(app.applicationContext).load(media.mediaURLHttps + ":small").placeholder(R.drawable.icon_loading).into(child)
                    val urls = arrayOfNulls<String>(mentitys.size)
                    mentitys.mapIndexed { j, it ->
                        urls[j] = it.mediaURLHttps
                    }
                    child.setOnClickListener {
                        val intent = Intent(context, ShowImageActivity::class.java).apply {
                            putExtra(ShowImageActivity.INTENT_EXTRA_KEY_URLS, urls)
                            putExtra(ShowImageActivity.INTENT_EXTRA_KEY_POSITION, index)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        } else {
            holder.tweetImagesScroll.visibility = View.GONE
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
        val tweetImagesScroll = binding.tweetImagesScroll
        val tweetImagesLayout = binding.tweetImagesLayout

        fun bind(viewModel: TweetViewModel, status: Status) {
            binding.also {
                it.viewModel = viewModel
                it.status = status
                it.executePendingBindings()
            }
        }
    }

}
