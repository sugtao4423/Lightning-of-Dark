package sugtao4423.lod.tweetlistview

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.loopj.android.image.SmartImageView
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.userpage_fragment.UserPage
import twitter4j.PagableResponseList
import twitter4j.User
import java.text.NumberFormat

class TweetListUserAdapter(private val context: Context) : RecyclerView.Adapter<TweetListUserAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private val data = ArrayList<User>()
    private val app = context.applicationContext as App
    private val handler = Handler()

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.list_item_tweet, viewGroup, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.size <= position) {
            return
        }
        val item = data[position]

        holder.rtIcon.visibility = View.GONE
        holder.rtSn.visibility = View.GONE
        holder.tweetImagesScroll.visibility = View.GONE
        holder.tweetImagesLayout.visibility = View.GONE

        if (item.isProtected) {
            holder.protect.apply {
                visibility = View.VISIBLE
                typeface = app.getFontAwesomeTypeface()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getOptions().userNameFontSize - 3)
            }
        } else {
            holder.protect.visibility = View.GONE
        }

        holder.icon.setImageUrl(item.biggerProfileImageURLHttps, null, R.drawable.icon_loading)
        holder.nameSn.text = item.name + " - @" + item.screenName
        holder.content.text = item.description
        val userCountsText = context.getString(R.string.param_user_count_detail,
                numberFormat(item.statusesCount),
                numberFormat(item.favouritesCount),
                numberFormat(item.friendsCount),
                numberFormat(item.followersCount)
        )
        holder.date.text = userCountsText

        holder.nameSn.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getOptions().userNameFontSize)
        holder.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getOptions().contentFontSize)
        holder.date.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getOptions().dateFontSize)

        holder.itemView.setOnClickListener {
            val i = Intent(context, UserPage::class.java)
            i.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, item.screenName)
            context.startActivity(i)
        }
    }

    private fun numberFormat(num: Int): String {
        return NumberFormat.getInstance().format(num)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun addAll(users: PagableResponseList<User>) {
        val pos = data.size
        data.addAll(users)
        handler.post {
            notifyItemRangeInserted(pos, users.size)
        }
    }

    fun clear() {
        val size = data.size
        data.clear()
        handler.post {
            notifyItemRangeRemoved(0, size)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: SmartImageView = itemView.findViewById(R.id.icon)
        val rtIcon: SmartImageView = itemView.findViewById(R.id.RetweetedUserIcon)
        val nameSn: TextView = itemView.findViewById(R.id.name_screenName)
        val content: TextView = itemView.findViewById(R.id.tweetText)
        val date: TextView = itemView.findViewById(R.id.tweet_date)
        val rtSn: TextView = itemView.findViewById(R.id.RetweetedUserScreenName)
        val protect: TextView = itemView.findViewById(R.id.UserProtected)
        val tweetImagesScroll: HorizontalScrollView = itemView.findViewById(R.id.tweet_images_scroll)
        val tweetImagesLayout: LinearLayout = itemView.findViewById(R.id.tweet_images_layout)
    }

}