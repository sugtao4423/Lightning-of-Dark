package sugtao4423.lod.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import sugtao4423.lod.R
import sugtao4423.lod.ui.userpage.UserPageActivity
import sugtao4423.lod.utils.ChromeIntent
import sugtao4423.lod.utils.Regex

fun Context.copyToClipboard(text: String, label: String = "text") {
    val clipboard = getSystemService<ClipboardManager>()
    val clip = ClipData.newPlainText(label, text)
    @Suppress("UsePropertyAccessSyntax") clipboard?.setPrimaryClip(clip)
}

fun ImageView.loadUri(uri: Uri?, placeholder: Drawable? = null) {
    if (uri == null) {
        if (placeholder == null) {
            setImageResource(R.drawable.icon_loading)
        } else {
            setImageDrawable(placeholder)
        }
        return
    }

    Glide.with(this).load(uri).let {
        if (placeholder == null) {
            it.placeholder(R.drawable.icon_loading)
        } else {
            it.placeholder(placeholder)
        }
    }.into(this)
}

fun ImageView.loadUrl(url: String?, placeholder: Drawable? = null) =
    loadUri(url?.toUri(), placeholder)

fun ImageView.loadWithThumbnail(
    originalUrl: String, thumbnailUrl: String?, requestListener: RequestListener<Drawable>
) {
    val glide = Glide.with(this)
    glide.load(originalUrl).let {
        if (thumbnailUrl != null) it.thumbnail(glide.load(thumbnailUrl)) else it
    }.listener(requestListener).into(this)
}

fun TextView.setLodLinkMovementString(string: String) {
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
                        startChrome(t.toUri())
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
