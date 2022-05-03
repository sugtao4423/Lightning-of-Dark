package sugtao4423.lod.ui.adapter.tweet.click

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.App
import sugtao4423.lod.databinding.DialogTweetBinding
import sugtao4423.lod.dialog_onclick.*
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.utils.ChromeIntent
import twitter4j.Status

class TweetItemDialog(context: Context, fontAwesomeTypeface: Typeface) {

    private val dialog: AlertDialog

    private val titleAdapter: TweetListAdapter
    private val listAdapter: ArrayAdapter<String>
    private val binding: DialogTweetBinding

    init {
        val tweetListView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        titleAdapter = TweetListAdapter(context).also {
            it.hideImages = true
            tweetListView.adapter = it
        }
        listAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)

        binding = DialogTweetBinding.inflate(LayoutInflater.from(context)).also {
            it.fontAwesomeTypeface = fontAwesomeTypeface
            it.listAdapter = listAdapter
        }

        dialog = AlertDialog.Builder(context).run {
            setCustomTitle(tweetListView)
            setView(binding.root)
            create()
        }
    }

    fun show(
        context: Context,
        status: Status,
        allStatusData: ArrayList<Status>,
        listStrings: List<String>
    ) {
        titleAdapter.apply {
            clear()
            add(status)
        }
        listAdapter.apply {
            clear()
            addAll(listStrings)
        }

        binding.selectList.onItemClickListener = Dialog_ListClick(status, allStatusData, dialog)
        binding.selectList.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { parent, _, position, _ ->
                val clickedText = parent.getItemAtPosition(position) as String
                if (clickedText.startsWith("http")) {
                    dialog.dismiss()
                    ChromeIntent(context, Uri.parse(clickedText))
                }
                true
            }

        binding.btnReply.setOnClickListener(Dialog_reply(status, context, dialog))
        binding.btnRetweet.apply {
            setOnClickListener(Dialog_retweet(status, context, dialog))
            setOnLongClickListener(Dialog_quoteRT(status, context, dialog))
        }
        binding.btnUnofficialRT.setOnClickListener(Dialog_unOfficialRT(status, context, dialog))
        binding.btnFavorite.setOnClickListener(Dialog_favorite(status, context, dialog))
        binding.btnTalk.apply {
            setOnClickListener(Dialog_talk(status, context, dialog))
            isEnabled = status.inReplyToStatusId > 0
        }
        binding.btnDelete.apply {
            setOnClickListener(Dialog_deletePost(status, context, dialog))
            isEnabled =
                (status.user.screenName == (context.applicationContext as App).account.screenName)
        }

        dialog.show()
    }

}
