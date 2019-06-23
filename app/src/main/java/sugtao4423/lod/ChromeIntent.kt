package sugtao4423.lod

import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat

class ChromeIntent(context: Context, uri: Uri) {

    init {
        CustomTabsIntent.Builder().apply {
            setShowTitle(true)
            enableUrlBarHiding()
            addDefaultShareMenuItem()
            setToolbarColor(ContextCompat.getColor(context, R.color.statusBar))
            build().launchUrl(context, uri)
        }
    }

}