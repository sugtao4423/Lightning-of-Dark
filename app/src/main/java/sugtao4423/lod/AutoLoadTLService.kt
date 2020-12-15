package sugtao4423.lod

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = applicationContext as App
        val listener = app.autoLoadTLListener
        val interval = app.getCurrentAccount().autoLoadTLInterval
        val listAsTL = app.getCurrentAccount().listAsTL

        val task = AutoLoadTLTask(app, listener, listAsTL)
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

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val channelId = "default"
        val title = "Running AutoLoadTL Service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
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

    inner class AutoLoadTLTask(private val app: App, private val listener: AutoLoadTLListener?, private val listAsTL: Long) : TimerTask() {

        override fun run() {
            try {
                val statuses = if (listAsTL > 0) {
                    app.getTwitter().getUserListStatuses(listAsTL, Paging(1, 50).sinceId(app.latestTweetId))
                } else {
                    app.getTwitter().getHomeTimeline(Paging(1, 50).sinceId(app.latestTweetId))
                }
                statuses.reverse()
                listener?.let {
                    handler.post {
                        it.onStatus(statuses)
                    }
                }
            } catch (e: TwitterException) {
            }
        }

    }

}