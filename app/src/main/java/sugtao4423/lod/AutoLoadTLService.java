package sugtao4423.lod;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class AutoLoadTLService extends Service{

    public interface AutoLoadTLListener{
        void onStatus(ResponseList<Status> statuses);
    }

    private Timer autoLoadTimer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        App app = (App)getApplicationContext();
        AutoLoadTLListener listener = app.getAutoLoadTLListener();
        int interval = app.getCurrentAccount().getAutoLoadTLInterval();
        long listAsTL = app.getCurrentAccount().getListAsTL();

        AutoLoadTLTask task = new AutoLoadTLTask(app, listener, listAsTL);
        autoLoadTimer = new Timer(true);
        autoLoadTimer.schedule(task, interval * 1000, interval * 1000);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startNotification(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(26)
    public void startNotification(Intent intent){
        Intent appIntent = new Intent(this, MainActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = "default";
        String title = "Running AutoLoadTL Service";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
        if(notificationManager != null){
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(getApplicationContext(), channelId)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.icon_notification)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy(){
        autoLoadTimer.cancel();
        autoLoadTimer.purge();
        super.onDestroy();
    }

    class AutoLoadTLTask extends TimerTask{

        private App app;
        private AutoLoadTLListener listener;
        private long listAsTL;

        public AutoLoadTLTask(App app, AutoLoadTLListener listener, long listAsTL){
            this.app = app;
            this.listener = listener;
            this.listAsTL = listAsTL;
        }

        @Override
        public void run(){
            try{
                ResponseList<Status> statuses;
                if(listAsTL > 0){
                    statuses = app.getTwitter().getUserListStatuses(listAsTL, new Paging(1, 50).sinceId(app.getLatestTweetId()));
                }else{
                    statuses = app.getTwitter().getHomeTimeline(new Paging(1, 50).sinceId(app.getLatestTweetId()));
                }
                Collections.reverse(statuses);
                listener.onStatus(statuses);
            }catch(TwitterException e){
            }
        }

    }

}
