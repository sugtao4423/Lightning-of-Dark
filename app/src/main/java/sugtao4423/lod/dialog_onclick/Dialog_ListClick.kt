package sugtao4423.lod.dialog_onclick

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import sugtao4423.lod.*
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity
import sugtao4423.lod.tweetlistview.TweetListAdapter
import sugtao4423.lod.tweetlistview.TweetListView
import sugtao4423.lod.userpage_fragment.UserPage
import sugtao4423.lod.utils.Regex
import sugtao4423.lod.utils.Utils
import twitter4j.Status
import java.util.regex.Pattern

class Dialog_ListClick(private val context: Context, private val status: Status, private val listData: ArrayList<Status>, private val dialog: AlertDialog) :
        AdapterView.OnItemClickListener {

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == null) {
            return
        }
        dialog.dismiss()
        val clickedText = parent.getItemAtPosition(position) as String

        if (clickedText == context.getString(R.string.extract_with_regex)) {
            val regView = View.inflate(context, R.layout.reg_dialog, null)
            val regEdit = regView.findViewById<EditText>(R.id.regDialog_edit)
            val gridLayout = regView.findViewById<GridLayout>(R.id.regDialog_grid)
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

            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            regEdit.setText(pref.getString(Keys.REGULAR_EXPRESSION, ""))
            AlertDialog.Builder(context).apply {
                setTitle(R.string.input_regex)
                setView(regView)
                setNegativeButton(R.string.cancel, null)
                setPositiveButton(R.string.ok) { _, _ ->
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(regEdit.windowToken, 0)

                    val editReg = regEdit.text.toString()
                    pref.edit().putString(Keys.REGULAR_EXPRESSION, editReg).commit()
                    val pattern = Pattern.compile(editReg, Pattern.DOTALL)
                    val adapter = TweetListAdapter(context)
                    var find = 0
                    listData.map {
                        if (pattern.matcher(it.text).find()) {
                            adapter.add(it)
                            find++
                        }
                    }
                    if (find == 0) {
                        ShowToast(context.applicationContext, R.string.nothing)
                    } else {
                        val l = TweetListView(context)
                        l.adapter = adapter
                        adapter.onItemClickListener = ListViewListener()
                        adapter.onItemLongClickListener = ListViewListener()
                        AlertDialog.Builder(context).setView(l).show()
                        val resultCount = context.getString(R.string.param_regex_result_count, listData.size, find)
                        ShowToast(context.applicationContext, resultCount, Toast.LENGTH_LONG)
                    }
                }
                show()
            }
        } else if (clickedText.startsWith("http") || clickedText.startsWith("ftp")) {
            val image = Regex.media_image.matcher(clickedText)
            val video = Regex.media_video.matcher(clickedText)
            val gif = Regex.media_gif.matcher(clickedText)
            val state = Regex.statusUrl.matcher(clickedText)
            val intent = when {
                image.find() -> {
                    val urls = arrayListOf<String>()
                    status.mediaEntities.map {
                        if (!Utils.isVideoOrGif(it)) {
                            urls.add(it.mediaURLHttps)
                        }
                    }
                    val pos = urls.indexOf(clickedText)
                    Intent(context, ImageFragmentActivity::class.java).apply {
                        putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, urls.toTypedArray())
                        putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_POSITION, pos)
                    }
                }
                video.find() -> {
                    Intent(context, Show_Video::class.java).apply {
                        putExtra(Show_Video.INTENT_EXTRA_KEY_URL, clickedText)
                        putExtra(Show_Video.INTENT_EXTRA_KEY_TYPE, Show_Video.TYPE_VIDEO)
                    }
                }
                gif.find() -> {
                    Intent(context, Show_Video::class.java).apply {
                        putExtra(Show_Video.INTENT_EXTRA_KEY_URL, clickedText)
                        putExtra(Show_Video.INTENT_EXTRA_KEY_TYPE, Show_Video.TYPE_GIF)
                    }
                }
                state.find() -> {
                    Intent(context, IntentActivity::class.java).apply {
                        putExtra(IntentActivity.TWEET_ID, state.group(Regex.statusUrlStatusIdGroup).toLong())
                    }
                }
                else -> {
                    ChromeIntent(context, Uri.parse(clickedText))
                    return
                }
            }
            context.startActivity(intent)
        } else if (clickedText == context.getString(R.string.open_in_browser)) {
            val orig = if (status.isRetweet) status.retweetedStatus else status
            val tweetSn = orig.user.screenName
            val tweetId = orig.id.toString()
            val url = "https://twitter.com/$tweetSn/status/$tweetId"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } else if (clickedText.startsWith("@")) {
            val intent = Intent(context, UserPage::class.java)
            val userSn = clickedText.substring(1)
            if (status.user.screenName == userSn) {
                intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_OBJECT, status.user)
            } else {
                intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, userSn)
            }
            context.startActivity(intent)
        }
    }

}