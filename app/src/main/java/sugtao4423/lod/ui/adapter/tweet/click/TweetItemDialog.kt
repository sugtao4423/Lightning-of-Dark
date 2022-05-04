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

        binding.apply {
            clickListeners = TweetDialogClickListeners(context, status, allStatusData) {
                dialog.dismiss()
            }
            talkButtonEnabled = status.inReplyToStatusId > 0
            deleteButtonEnabled =
                (status.user.screenName == (context.applicationContext as App).account.screenName)
        }
        dialog.show()
    }

}
