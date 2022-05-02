package sugtao4423.lod.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ui.showvideo.ShowVideoActivity
import sugtao4423.lod.StatusClickListener
import sugtao4423.lod.ui.showimage.ShowImageActivity
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.utils.Utils
import twitter4j.Status
import java.text.SimpleDateFormat
import java.util.*

class TweetListAdapter(val context: Context) : RecyclerView.Adapter<TweetListAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private val app = context.applicationContext as App
    private val statusDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss" + (if (app.prefRepository.isMillisecond) ".SSS" else ""), Locale.getDefault())
    private val statusClickListener = StatusClickListener()
    val data = arrayListOf<Status>()
    var hasNextPage = true
    var hideImages = false

    interface OnItemClickListener {
        fun onItemClicked(tweetListAdapter: TweetListAdapter, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(tweetListAdapter: TweetListAdapter, position: Int): Boolean
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.list_item_tweet, viewGroup, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.size <= position) {
            return
        }
        val item = data[position]
        val origStatus = if (item.isRetweet) item.retweetedStatus else item

        // 鍵
        if (origStatus.user.isProtected) {
            holder.protect.typeface = app.fontAwesomeTypeface
            holder.protect.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.userNameFontSize - 3)
            holder.protect.visibility = View.VISIBLE
        } else {
            holder.protect.visibility = View.GONE
        }

        // アイコン、名前、スクリーンネーム、タイムスタンプ、クライアント
        if (item.isRetweet) {
            holder.rtIcon.visibility = View.VISIBLE
            holder.rtSn.visibility = View.VISIBLE
            val date = statusDateFormat.format(Date((item.retweetedStatus.id shr 22) + 1288834974657L))
            holder.date.text = "$date  Retweeted by "
            Glide.with(app.applicationContext).load(item.user.profileImageURLHttps).placeholder(R.drawable.icon_loading).into(holder.rtIcon)
            holder.rtSn.text = "@" + item.user.screenName
        } else {
            holder.rtIcon.visibility = View.GONE
            holder.rtSn.visibility = View.GONE
            val date = statusDateFormat.format(Date((item.id shr 22) + 1288834974657L))
            holder.date.text = "$date  via " + item.source.replace(Regex("<.+?>"), "")
        }

        holder.nameSn.text = origStatus.user.name + " - @" + origStatus.user.screenName
        holder.content.text = origStatus.text
        Glide.with(app.applicationContext).load(origStatus.user.biggerProfileImageURLHttps).placeholder(R.drawable.icon_loading).into(holder.icon)

        holder.nameSn.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.userNameFontSize)
        holder.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.contentFontSize)
        holder.date.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.dateFontSize)
        if (item.isRetweet) {
            holder.rtSn.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.dateFontSize)
        }

        holder.icon.setOnClickListener {
            val intent = Intent(context, UserPageActivity::class.java)
            intent.putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_OBJECT, origStatus.user)
            context.startActivity(intent)
        }

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
            holder.tweetImagesScroll.setOnTouchListener { v, event ->
                val sv = v as HorizontalScrollView
                if (sv.getChildAt(0).width > sv.width) {
                    v.onTouchEvent(event)
                } else {
                    holder.itemView.onTouchEvent(event)
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.tweetIcon)
        val rtIcon: ImageView = itemView.findViewById(R.id.retweetedUserIcon)
        val nameSn: TextView = itemView.findViewById(R.id.tweetNameScreenName)
        val content: TextView = itemView.findViewById(R.id.tweetText)
        val date: TextView = itemView.findViewById(R.id.tweetDate)
        val rtSn: TextView = itemView.findViewById(R.id.retweetedUserScreenName)
        val protect: TextView = itemView.findViewById(R.id.tweetUserProtected)
        val tweetImagesScroll: HorizontalScrollView = itemView.findViewById(R.id.tweetImagesScroll)
        val tweetImagesLayout: LinearLayout = itemView.findViewById(R.id.tweetImagesLayout)
    }

}
