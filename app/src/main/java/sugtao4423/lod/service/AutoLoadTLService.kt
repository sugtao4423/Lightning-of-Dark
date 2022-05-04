package sugtao4423.lod.service

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ui.main.MainActivity
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterException
import java.util.*

class AutoLoadTLService : Service() {

    interface AutoLoadTLListener {
        fun onStatus(statuses: ResponseList<Status>)
    }

    private lateinit var autoLoadTimer: Timer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = applicationContext as App
        val interval = app.account.autoLoadTLInterval

        val task = AutoLoadTLTask(app)
        autoLoadTimer = Timer(true)
        autoLoadTimer.schedule(task, interval * 1000L, interval * 1000L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startNotification()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun startNotification() {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val channelId = "default"
        val title = "Running AutoLoadTL Service"
        NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT).let {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(it)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(applicationContext, channelId).run {
            setContentTitle(title)
            setSmallIcon(R.drawable.icon_notification)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            setWhen(System.currentTimeMillis())
            build()
        }
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        autoLoadTimer.cancel()
        autoLoadTimer.purge()
        super.onDestroy()
    }

    inner class AutoLoadTLTask(private val app: App) : TimerTask() {

        override fun run() {
            try {
                val paging = Paging(1, 50).sinceId(app.latestTweetId)
                val statuses = if (app.account.listAsTL > 0) {
                    app.twitter.getUserListStatuses(app.account.listAsTL, paging)
                } else {
                    app.twitter.getHomeTimeline(paging)
                }
                app.autoLoadTLListener?.onStatus(statuses)
            } catch (e: TwitterException) {
            }
        }

    }

}
