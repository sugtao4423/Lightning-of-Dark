package sugtao4423.lod.playing_music_data;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

import java.util.HashMap;
import java.util.List;

import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;

public class PlayingMusicData{

    private Context context;
    private MediaSessionManager mediaSessionManager;

    public PlayingMusicData(Context context){
        this.context = context;
        mediaSessionManager = (MediaSessionManager)context.getSystemService(Context.MEDIA_SESSION_SERVICE);
    }

    public HashMap<MusicDataKey, String> getPlayingMusicData(){
        if(!hasNotificationAccessPermission()){
            requestNotificationAccessPermission();
            return null;
        }

        HashMap<MusicDataKey, String> map = new HashMap<>();
        List<MediaController> controllers = mediaSessionManager.getActiveSessions(new ComponentName(context, MusicNotificationListener.class));
        for(MediaController mc : controllers){
            if(mc == null || mc.getPlaybackState() == null || mc.getMetadata() == null){
                continue;
            }
            boolean playing = (PlaybackState.STATE_PLAYING == mc.getPlaybackState().getState());
            if(playing){
                String title = mc.getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE);
                String artist = mc.getMetadata().getString(MediaMetadata.METADATA_KEY_ARTIST);
                String album = mc.getMetadata().getString(MediaMetadata.METADATA_KEY_ALBUM);
                map.put(MusicDataKey.TITLE, title);
                map.put(MusicDataKey.ARTIST, artist);
                map.put(MusicDataKey.ALBUM, album);
                return map;
            }
        }
        return null;
    }

    private boolean hasNotificationAccessPermission(){
        return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.getPackageName());
    }

    private void requestNotificationAccessPermission(){
        new AlertDialog.Builder(context)
                .setMessage(R.string.permission_notification_access_message)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        new ShowToast(context.getApplicationContext(), R.string.permission_rejected);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String action;
                        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP){
                            action = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
                        }else{
                            action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                        }
                        context.startActivity(new Intent(action));
                    }
                })
                .show();
    }

}
