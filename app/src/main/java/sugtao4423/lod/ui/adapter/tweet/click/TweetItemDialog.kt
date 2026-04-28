package sugtao4423.lod.ui.adapter.tweet.click

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sugtao4423.lod.App
import sugtao4423.lod.databinding.DialogTweetBinding
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.adapter.tweet.click.listener.TweetDialogClickListeners
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

        binding = DialogTweetBinding.inflate(LayoutInflater.from(context))
        binding.apply {
            listView.adapter = listAdapter
            replyButton.typeface = fontAwesomeTypeface
            retweetButton.typeface = fontAwesomeTypeface
            quoteButton.typeface = fontAwesomeTypeface
            favoriteButton.typeface = fontAwesomeTypeface
            talkButton.typeface = fontAwesomeTypeface
            deleteButton.typeface = fontAwesomeTypeface
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

        binding.apply {
            val clickListeners = TweetDialogClickListeners(context, status, allStatusData) {
                dialog.dismiss()
            }

            listView.onItemClickListener = clickListeners.listItemClickListener
            listView.onItemLongClickListener = clickListeners.listItemClickListener
            replyButton.setOnClickListener(clickListeners.replyListener)
            retweetButton.setOnClickListener(clickListeners.retweetListener)
            retweetButton.setOnLongClickListener(clickListeners.quoteRTListener)
            quoteButton.setOnClickListener(clickListeners.unOfficialRTListener)
            favoriteButton.setOnClickListener(clickListeners.favoriteListener)
            talkButton.isEnabled = status.inReplyToStatusId > 0
            talkButton.setOnClickListener(clickListeners.talkListener)
            deleteButton.isEnabled =
                (status.user.id == (context.applicationContext as App).account.id)
            deleteButton.setOnClickListener(clickListeners.deleteTweetListener)
        }

        dialog.show()
    }

}
