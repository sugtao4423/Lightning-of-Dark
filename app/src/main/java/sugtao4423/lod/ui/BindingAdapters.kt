package sugtao4423.lod.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import sugtao4423.lod.ChromeIntent
import sugtao4423.lod.R
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.utils.Regex

@BindingAdapter("android:visibility")
fun View.visibility(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("colorSchemeResources")
fun SwipeRefreshLayout.colorSchemeColors(colorResIds: IntArray) {
    setColorSchemeColors(*colorResIds)
}

@BindingAdapter("imageUrl", "placeholder", requireAll = false)
fun ImageView.imageUrl(url: String?, placeholder: Drawable?) {
    if (url.isNullOrEmpty()) {
        if (placeholder == null) {
            setImageResource(R.drawable.icon_loading)
        } else {
            setImageDrawable(placeholder)
        }
        return
    }

    Glide.with(this).load(url).let {
        if (placeholder == null) {
            it.placeholder(R.drawable.icon_loading)
        } else {
            it.placeholder(placeholder)
        }
    }.into(this)
}

@BindingAdapter("imageUrl", "imageListener")
fun ImageView.imageUrl(url: String?, imageListener: RequestListener<Drawable>) {
    Glide.with(this).load(url).listener(imageListener).into(this)
}

@BindingAdapter("isSelectionEnd")
fun EditText.isSelectionEnd(isSelectionEnd: Boolean?) {
    if (isSelectionEnd == true) {
        setSelection(text.length)
    }
}

@BindingAdapter("lodLinkMovementString")
fun TextView.lodLinkMovementString(string: String?) {
    if (string.isNullOrEmpty()) {
        text = ""
        return
    }

    fun startUserPage(screenName: String) {
        val intent = Intent(context, UserPageActivity::class.java)
        intent.putExtra(UserPageActivity.INTENT_EXTRA_KEY_USER_SCREEN_NAME, screenName)
        context.startActivity(intent)
    }

    fun startChrome(uri: Uri) = ChromeIntent(context, uri)

    val ss = SpannableString(string)
    val m = Regex.userAndAnyUrl.matcher(string)
    while (m.find()) {
        val t = m.group()
        val urlSpan = if (t.startsWith("@") || t.startsWith("http")) {
            object : URLSpan(t) {
                override fun onClick(widget: View) {
                    if (t.startsWith("@")) {
                        startUserPage(url.replace("@", ""))
                    } else if (t.startsWith("http")) {
                        startChrome(Uri.parse(t))
                    }
                }
            }
        } else {
            URLSpan(t)
        }
        ss.setSpan(urlSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    text = ss
    movementMethod = LinkMovementMethod.getInstance()
}
