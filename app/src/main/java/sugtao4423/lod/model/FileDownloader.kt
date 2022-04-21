package sugtao4423.lod.model

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment

class FileDownloader(private val application: Application) {

    fun download(uri: Uri, fileName: String, onDownloaded: () -> Unit) {
        val dlManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                onDownloaded()
                application.unregisterReceiver(this)
            }
        }
        application.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        val dlRequest = DownloadManager.Request(uri).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        dlManager.enqueue(dlRequest)
    }

}
