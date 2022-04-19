package sugtao4423.lod.dialog_onclick

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import sugtao4423.lod.*
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.utils.Regex
import sugtao4423.lod.utils.Utils
import twitter4j.Status
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class Dialog_ListClick(private val status: Status, private val listData: ArrayList<Status>, private val dialog: AlertDialog) :
        AdapterView.OnItemClickListener {

    private lateinit var context: Context

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == null) {
            return
        }
        dialog.dismiss()
        context = parent.context
        val clickedText = parent.getItemAtPosition(position) as String

        when {
            clickedText == context.getString(R.string.extract_with_regex) -> extractTweetsWithRegex()
            clickedText.contains(Regex("^(http|ftp)")) -> openUrl(clickedText)
            clickedText == context.getString(R.string.open_in_browser) -> openInBrowser()
            clickedText.startsWith("@") -> openUserPage(clickedText.substring(1))
        }
    }

    private fun extractTweetsWithRegex() {
        val regView = View.inflate(context, R.layout.reg_dialog, null)
        val regEdit = regView.findViewById<EditText>(R.id.regDialogEdit)
        val regIncludeRT = regView.findViewById<CheckBox>(R.id.regDialogIncludeRetweet)
        val gridLayout = regView.findViewById<GridLayout>(R.id.regDialogGrid)
        val regItems = arrayOf(".", "*", "|", "+", "?", "\\", "^", "$", "(", ")", "[", "]", "{", "}")
        val params = LinearLayout.LayoutParams(130, 130)
        regItems.map {
            val btn = Button(context).apply {
                layoutParams = params
                text = it
                setOnClickListener(Dialog_regButtonClick(regEdit))
            }
            gridLayout.addView(btn)
        }

        val prefRepo = (context.applicationContext as App).prefRepository
        regEdit.setText(prefRepo.regularExpression)
        AlertDialog.Builder(context).also {
            it.setTitle(R.string.input_regex)
            it.setView(regView)
            it.setNegativeButton(R.string.cancel, null)
            it.setPositiveButton(R.string.ok) { _, _ ->
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(regEdit.windowToken, 0)

                val editReg = regEdit.text.toString()
                prefRepo.regularExpression = editReg
                val pattern: Pattern
                try {
                    pattern = Pattern.compile(editReg, Pattern.DOTALL)
                } catch (e: PatternSyntaxException) {
                    ShowToast(context.applicationContext, R.string.invalid_pattern)
                    return@setPositiveButton
                }
                val adapter = TweetListAdapter(context)
                adapter.addAll(listData.filter { status ->
                    pattern.matcher(status.text).find() &&
                            (regIncludeRT.isChecked || (!regIncludeRT.isChecked && !status.isRetweet))
                })
                if (adapter.itemCount == 0) {
                    ShowToast(context.applicationContext, R.string.nothing)
                } else {
                    val l = TweetListView(context)
                    l.adapter = adapter
                    AlertDialog.Builder(context).setView(l).show()
                    ShowToast(context.applicationContext, R.string.param_regex_result_count, listData.size, adapter.itemCount)
                }
            }
            it.show()
        }
    }

    private fun openUrl(urlText: String) {
        val image = Regex.mediaImage.matcher(urlText)
        val video = Regex.mediaVideo.matcher(urlText)
        val gif = Regex.mediaGif.matcher(urlText)
        val state = Regex.statusUrl.matcher(urlText)
        val intent = when {
            image.find() -> {
                val urls = arrayListOf<String>()
                status.mediaEntities.map {
                    if (!Utils.isVideoOrGif(it)) {
                        urls.add(it.mediaURLHttps)
                    }
                }
                val pos = urls.indexOf(urlText)
                Intent(context, ImageFragmentActivity::class.java).apply {
                    putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, urls.toTypedArray())
                    putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_POSITION, pos)
                }
            }
            video.find() -> {
                Intent(context, ShowVideo::class.java).apply {
                    putExtra(ShowVideo.INTENT_EXTRA_KEY_URL, urlText)
                    putExtra(ShowVideo.INTENT_EXTRA_KEY_TYPE, ShowVideo.TYPE_VIDEO)
                }
            }
            gif.find() -> {
                Intent(context, ShowVideo::class.java).apply {
                    putExtra(ShowVideo.INTENT_EXTRA_KEY_URL, urlText)
                    putExtra(ShowVideo.INTENT_EXTRA_KEY_TYPE, ShowVideo.TYPE_GIF)
                }
            }
            state.find() -> {
                Intent(context, IntentActivity::class.java).apply {
                    putExtra(IntentActivity.TWEET_ID, state.group(Regex.statusUrlStatusIdGroup)!!.toLong())
                }
            }
            else -> {
                ChromeIntent(context, Uri.parse(urlText))
                return
            }
        }
        context.startActivity(intent)
    }

    private fun openInBrowser() {
        val orig = if (status.isRetweet) status.retweetedStatus else status
        val tweetSn = orig.user.screenName
        val tweetId = orig.id.toString()
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
