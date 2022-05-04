package sugtao4423.lod.utils

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import androidx.core.content.ContextCompat
import sugtao4423.lod.R

class ChromeIntent(context: Context, uri: Uri) {

    init {
        CustomTabsIntent.Builder().apply {
            setShowTitle(true)
            setUrlBarHidingEnabled(true)
            setShareState(SHARE_STATE_ON)
            val colorScheme = CustomTabColorSchemeParams.Builder().let {
                it.setToolbarColor(ContextCompat.getColor(context, R.color.statusBar))
                it.build()
            }
            setDefaultColorSchemeParams(colorScheme)
            build().launchUrl(context, uri)
        }
    }

}
