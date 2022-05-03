package sugtao4423.lod.ui.adapter.tweet.click

import android.content.Context
import android.graphics.Typeface
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
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.dialog_onclick.*
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.utils.ChromeIntent
import twitter4j.Status

class TweetItemDialog(context: Context, fontAwesomeTypeface: Typeface) {

    private val dialog: AlertDialog

    private val adapter: TweetListAdapter
    private val dialogList: ListView
    private val dialogBtn: List<Button>

    init {
        val tweetListView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        adapter = TweetListAdapter(context).also {
            it.hideImages = true
            tweetListView.adapter = it
        }

        val view = View.inflate(context, R.layout.custom_dialog, null)
        dialogList = view.findViewById(R.id.dialog_List)
        dialogBtn = listOf(
            view.findViewById(R.id.dialogReply),
            view.findViewById(R.id.dialogRetweet),
            view.findViewById(R.id.dialogUnofficialRT),
            view.findViewById(R.id.dialogFavorite),
            view.findViewById(R.id.dialogTalk),
            view.findViewById(R.id.dialogDelete)
        )

        val density = context.resources.displayMetrics.density
        val black = ContextCompat.getColor(context, R.color.icon)
        dialogBtn.forEach {
            it.typeface = fontAwesomeTypeface
            it.textSize = 9 * density
            it.setTextColor(black)
        }

        dialog = AlertDialog.Builder(context).run {
            setCustomTitle(tweetListView)
            setView(view)
            create()
        }
    }

    fun show(
        context: Context,
        status: Status,
        allStatusData: ArrayList<Status>,
        listStrings: List<String>
    ) {
        adapter.apply {
            clear()
            add(status)
        }
        dialog.show()

        dialogList.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, listStrings)
        dialogList.onItemClickListener = Dialog_ListClick(status, allStatusData, dialog)
        dialogList.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { parent, _, position, _ ->
                val clickedText = parent.getItemAtPosition(position) as String
                if (clickedText.startsWith("http")) {
                    dialog.dismiss()
                    ChromeIntent(context, Uri.parse(clickedText))
                }
                true
            }

        dialogBtn[0].setOnClickListener(Dialog_reply(status, context, dialog))
        dialogBtn[1].apply {
            setOnClickListener(Dialog_retweet(status, context, dialog))
            setOnLongClickListener(Dialog_quoteRT(status, context, dialog))
        }
        dialogBtn[2].setOnClickListener(Dialog_unOfficialRT(status, context, dialog))
        dialogBtn[3].setOnClickListener(Dialog_favorite(status, context, dialog))
        dialogBtn[4].apply {
            setOnClickListener(Dialog_talk(status, context, dialog))
            isEnabled = status.inReplyToStatusId > 0
        }
        dialogBtn[5].apply {
            setOnClickListener(Dialog_deletePost(status, context, dialog))
            isEnabled =
                (status.user.screenName == (context.applicationContext as App).account.screenName)
        }
    }

}
