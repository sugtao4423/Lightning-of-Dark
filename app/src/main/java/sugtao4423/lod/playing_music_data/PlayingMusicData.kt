package sugtao4423.lod.playing_music_data

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast

class PlayingMusicData(private val context: Context) {

    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    fun getPlayingMusicData(): HashMap<MusicDataKey, String>? {
        if (!hasNotificationAccessPermission()) {
            requestNotificationAccessPermission()
            return null
        }

        val map = HashMap<MusicDataKey, String>()
        val controllers = mediaSessionManager.getActiveSessions(ComponentName(context, MusicNotificationListener::class.java))
        controllers.map {
            if (it == null || it.playbackState == null || it.metadata == null) {
                return@map
            }
            val playing = (PlaybackState.STATE_PLAYING == it.playbackState!!.state)
            if (playing) {
                val title = it.metadata!!.getString(MediaMetadata.METADATA_KEY_TITLE)
                val artist = it.metadata!!.getString(MediaMetadata.METADATA_KEY_ARTIST)
                val album = it.metadata!!.getString(MediaMetadata.METADATA_KEY_ALBUM)
                map[MusicDataKey.TITLE] = title
                map[MusicDataKey.ARTIST] = artist
                map[MusicDataKey.ALBUM] = album
                return map
            }
        }
        return null
    }

    private fun hasNotificationAccessPermission(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }

    private fun requestNotificationAccessPermission() {
        AlertDialog.Builder(context).apply {
            setMessage(R.string.permission_notification_access_message)
            setNegativeButton(R.string.cancel) { _, _ ->
                ShowToast(context.applicationContext, R.string.permission_rejected)
            }
            setPositiveButton(R.string.ok) { _, _ ->
                val action = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
                } else {
                    Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                }
                context.startActivity(Intent(action))
            }
            show()
        }
    }

}