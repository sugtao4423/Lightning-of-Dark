package sugtao4423.lod.model

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

class FileDownloader(private val context: Context) {

    fun download(uri: Uri, fileName: String, onDownloaded: () -> Unit) {
        val dlManager = context.getSystemService<DownloadManager>() ?: return
        var downloadId = -1L

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id != downloadId) return

                context.unregisterReceiver(this)
                onDownloaded()
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )

        val dlRequest = DownloadManager.Request(uri).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        downloadId = dlManager.enqueue(dlRequest)
    }

}
