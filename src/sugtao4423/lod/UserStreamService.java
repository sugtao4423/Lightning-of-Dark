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
import twitter4j.TwitterStream;

public class UserStreamService extends Service{

	private TwitterStream twitterStream;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		App app = (App)getApplicationContext();
		twitterStream = app.getTwitterStream();
		twitterStream.addListener(app.getUserStreamAdapter());
		twitterStream.addConnectionLifeCycleListener(app.getConnectionLifeCycleListener());
		twitterStream.user();
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
		String title = "Connecting UserStream";
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
		if(notificationManager != null){
			notificationManager.createNotificationChannel(channel);
			Notification notification = new Notification.Builder(getApplicationContext(), channelId)
					.setContentTitle(title)
					.setSmallIcon(R.drawable.notification_icon)
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
		twitterStream.shutdown();
		super.onDestroy();
	}

}
