package sugtao4423.lod.ui.adapter.tweet.click.listener

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.databinding.DialogRegexBinding
import sugtao4423.lod.ui.adapter.converter.TweetViewDataConverter
import sugtao4423.lod.ui.adapter.tweet.TweetListAdapter
import sugtao4423.lod.ui.intent.IntentActivity
import sugtao4423.lod.ui.showimage.ShowImageActivity
import sugtao4423.lod.ui.showvideo.ShowVideoActivity
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.utils.ChromeIntent
import sugtao4423.lod.utils.showToast
import sugtao4423.lod.view.TweetListView
import twitter4j.Status
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class ListItemClickListener(
    private val status: Status,
    private val allStatusData: ArrayList<Status>,
    private val onClicked: () -> Unit,
) : AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private lateinit var context: Context

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == null) return

        onClicked()
        context = parent.context
        val clickedText = parent.getItemAtPosition(position) as String
        when {
            clickedText == context.getString(R.string.extract_with_regex) -> extractTweetsWithRegex()
            clickedText.contains(Regex("^(http|ftp)")) -> openUrl(clickedText)
            clickedText == context.getString(R.string.open_in_browser) -> openInBrowser()
            clickedText.startsWith("@") -> openUserPage(clickedText.substring(1))
        }
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ): Boolean {
        if (parent == null) return true

        onClicked()
        val clickedText = parent.getItemAtPosition(position) as String
        if (clickedText.startsWith("http")) {
            ChromeIntent(context, Uri.parse(clickedText))
        }
        return true
    }

    private fun closeKeyboard(v: View) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun extractTweetsWithRegex() {
        val pref = (context.applicationContext as App).prefRepository

        val regexText = MutableLiveData(pref.regularExpression)
        val isIncludeRetweet = MutableLiveData(true)
        val binding = DialogRegexBinding.inflate(LayoutInflater.from(context)).also {
            it.regexText = regexText
            it.isIncludeRetweet = isIncludeRetweet
            it.regButtonListener = RegButtonClickListener(it.regDialogEdit)
        }

        AlertDialog.Builder(context).also {
            it.setTitle(R.string.input_regex)
            it.setView(binding.root)
            it.setNegativeButton(R.string.cancel, null)
            it.setPositiveButton(R.string.ok) { _, _ ->
                closeKeyboard(binding.root)
                pref.regularExpression = regexText.value!!
                showRegexFilterResult(regexText.value!!, isIncludeRetweet.value!!)
            }
            it.show()
        }
    }

    private fun showRegexFilterResult(regexText: String, isIncludeRetweet: Boolean) {
        val pattern: Pattern
        try {
            pattern = Pattern.compile(regexText, Pattern.DOTALL)
        } catch (e: PatternSyntaxException) {
            context.showToast(R.string.invalid_pattern)
            return
        }
        val filtered = allStatusData.filter {
            pattern.matcher(it.text).find() &&
                    (isIncludeRetweet || (!isIncludeRetweet && !it.isRetweet))
        }
        if (filtered.isEmpty()) {
            context.showToast(R.string.nothing)
            return
        }

        val l = TweetListView(context).also {
            it.adapter = TweetListAdapter(context).apply { addAll(filtered) }
        }
        AlertDialog.Builder(context).setView(l).show()
        context.showToast(
            R.string.param_regex_result_count,
            allStatusData.size,
            filtered.size
        )
    }

    private fun openUrl(urlText: String) {
        val image = sugtao4423.lod.utils.Regex.mediaImage.matcher(urlText)
        val video = sugtao4423.lod.utils.Regex.mediaVideo.matcher(urlText)
        val gif = sugtao4423.lod.utils.Regex.mediaGif.matcher(urlText)
        val state = sugtao4423.lod.utils.Regex.statusUrl.matcher(urlText)
        val intent = when {
            image.find() -> Intent(context, ShowImageActivity::class.java).apply {
                val urls = TweetViewDataConverter.allImageUrls(status.mediaEntities.toList())
                val pos = urls.indexOf(urlText)
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_URLS, urls.toTypedArray())
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_POSITION, pos)
            }
            video.find() -> Intent(context, ShowVideoActivity::class.java).apply {
                putExtra(ShowVideoActivity.INTENT_EXTRA_KEY_URL, urlText)
                putExtra(ShowVideoActivity.INTENT_EXTRA_KEY_TYPE, ShowVideoActivity.TYPE_VIDEO)
            }
            gif.find() -> Intent(context, ShowVideoActivity::class.java).apply {
                putExtra(ShowVideoActivity.INTENT_EXTRA_KEY_URL, urlText)
                putExtra(ShowVideoActivity.INTENT_EXTRA_KEY_TYPE, ShowVideoActivity.TYPE_GIF)
            }
            state.find() -> Intent(context, IntentActivity::class.java).apply {
                putExtra(
                    IntentActivity.TWEET_ID,
                    state.group(sugtao4423.lod.utils.Regex.statusUrlStatusIdGroup)!!.toLong()
                )
            }
            else -> {
                ChromeIntent(context, Uri.parse(urlText))
                return
            }
        }
        context.startActivity(intent)
    }

    private fun openInBrowser() {
        val tweetSn = status.user.screenName
        val tweetId = status.id.toString()
        val url = "https://twitter.com/$tweetSn/status/$tweetId"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun openUserPage(userScreenName: String) {
        val intent = Intent(context, UserPageActivity::class.java)
        if (status.user.screenName == userScreenName) {
            intent.putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_OBJECT, status.user)
        } else {
            intent.putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_SCREEN_NAME, userScreenName)
        }
        context.startActivity(intent)
    }

}
