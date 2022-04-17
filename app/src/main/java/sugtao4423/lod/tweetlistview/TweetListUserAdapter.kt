package sugtao4423.lod.tweetlistview

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
                typeface = app.fontAwesomeTypeface
                setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.userNameFontSize - 3)
            }
        } else {
            holder.protect.visibility = View.GONE
        }

        Glide.with(context).load(item.biggerProfileImageURLHttps).placeholder(R.drawable.icon_loading).into(holder.icon)
        holder.nameSn.text = item.name + " - @" + item.screenName
        holder.content.text = item.description
        val userCountsText = context.getString(R.string.param_user_count_detail,
                numberFormat(item.statusesCount),
                numberFormat(item.favouritesCount),
                numberFormat(item.friendsCount),
                numberFormat(item.followersCount)
        )
        holder.date.text = userCountsText

        holder.nameSn.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.userNameFontSize)
        holder.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.contentFontSize)
        holder.date.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.prefRepository.dateFontSize)

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
        notifyItemRangeInserted(pos, users.size)
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
