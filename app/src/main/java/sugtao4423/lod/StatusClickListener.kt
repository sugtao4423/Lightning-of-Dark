package sugtao4423.lod

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.dialog_onclick.*
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.utils.Utils
import twitter4j.Status

class StatusClickListener : TweetListAdapter.OnItemClickListener, TweetListAdapter.OnItemLongClickListener {

    override fun onItemClicked(tweetListAdapter: TweetListAdapter, position: Int) {
        val item = tweetListAdapter.data[position]
        val context = tweetListAdapter.context
        val app = context.applicationContext as App

        val list = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)
        if (app.getOptions().isRegex) {
            list.add(context.getString(R.string.extract_with_regex))
        }
        if (app.getOptions().isOpenBrowser) {
            list.add(context.getString(R.string.open_in_browser))
        }

        val users = arrayListOf<String>()
        users.add("@${item.user.screenName}")

        item.userMentionEntities.map {
            if (!users.contains("@${it.screenName}")) {
                users.add("@${it.screenName}")
            }
        }
        list.addAll(users)

        item.urlEntities.map {
            list.add(it.expandedURL)
        }

        item.mediaEntities.map {
            if (Utils.isVideoOrGif(it)) {
                val videoUrl = Utils.getHiBitrateVideoUrl(item.mediaEntities)
                if (videoUrl == null) {
                    list.add(context.getString(R.string.error_get_video))
                } else {
                    list.add(videoUrl)
                }
            } else {
                list.add(it.mediaURLHttps)
            }
        }

        val status = if (item.isRetweet) item.retweetedStatus else item
        showDialog(context, status, tweetListAdapter.data, list)
    }

    override fun onItemLongClicked(tweetListAdapter: TweetListAdapter, position: Int): Boolean {
        val i = Intent(tweetListAdapter.context, TweetActivity::class.java).apply {
            putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_PAKUTSUI)
            putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, tweetListAdapter.data[position])
        }
        tweetListAdapter.context.startActivity(i)
        return true
    }

    private lateinit var adapter: TweetListAdapter

    private lateinit var dialogList: ListView
    private lateinit var dialogBtn: Array<Button>

    private var dialog: AlertDialog? = null

    private fun createDialog(context: Context) {
        val tweetListView = RecyclerView(context)
        val llm = LinearLayoutManager(context)
        tweetListView.layoutManager = llm
        adapter = TweetListAdapter(context).also {
            it.hideImages = true
            tweetListView.adapter = it
        }

        val content = View.inflate(context, R.layout.custom_dialog, null)
        dialogList = content.findViewById(R.id.dialog_List)
        dialogBtn = arrayOf(
                content.findViewById(R.id.dialogReply),
                content.findViewById(R.id.dialogRetweet),
                content.findViewById(R.id.dialogUnofficialRT),
                content.findViewById(R.id.dialogFavorite),
                content.findViewById(R.id.dialogTalk),
                content.findViewById(R.id.dialogDelete)
        )

        val tf = (context.applicationContext as App).getFontAwesomeTypeface()
        val density = context.resources.displayMetrics.density
        val black = ContextCompat.getColor(context, R.color.icon)
        dialogBtn.map {
            it.typeface = tf
            it.textSize = 9 * density
            it.setTextColor(black)
        }

        dialog = AlertDialog.Builder(context).run {
            setCustomTitle(tweetListView)
            setView(content)
            create()
        }
    }

    private fun showDialog(context: Context, status: Status, allStatusData: ArrayList<Status>, listStrings: ArrayAdapter<String>) {
        if (dialog == null) {
            createDialog(context)
        }
        adapter.clear()
        adapter.add(status)
        dialog!!.show()

        dialogList.adapter = listStrings
        dialogList.onItemClickListener = Dialog_ListClick(status, allStatusData, dialog!!)
        dialogList.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, _, position, _ ->
            val clickedText = parent.getItemAtPosition(position) as String
            if (clickedText.startsWith("http")) {
                dialog!!.dismiss()
                ChromeIntent(context, Uri.parse(clickedText))
            }
            true
        }

        dialogBtn[0].setOnClickListener(Dialog_reply(status, context, dialog!!))
        dialogBtn[1].setOnClickListener(Dialog_retweet(status, context, dialog!!))
        dialogBtn[1].setOnLongClickListener(Dialog_quoteRT(status, context, dialog!!))
        dialogBtn[2].setOnClickListener(Dialog_unOfficialRT(status, context, dialog!!))
        dialogBtn[3].setOnClickListener(Dialog_favorite(status, context, dialog!!))
        dialogBtn[4].setOnClickListener(Dialog_talk(status, context, dialog!!))
        dialogBtn[5].setOnClickListener(Dialog_deletePost(status, context, dialog!!))

        dialogBtn[4].isEnabled = status.inReplyToStatusId > 0
        dialogBtn[5].isEnabled = (status.user.screenName == (context.applicationContext as App).getCurrentAccount().screenName)
    }

}