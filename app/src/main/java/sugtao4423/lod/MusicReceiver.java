package sugtao4423.lod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Arrays;

import sugtao4423.lod.dataclass.Music;

public class MusicReceiver extends BroadcastReceiver{

    public static final String[] ACTIONS_GOOGLEPLAY = new String[]{
            "com.android.music.metachanged",
            "com.android.music.playstatechanged",
            "com.android.music.playbackcomplete"
    };

    public static final String[] ACTIONS_SONYMUSIC = new String[]{
            "com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED",
            "com.sonyericsson.music.playbackcontrol.ACTION_PAUSED",
            "com.sonyericsson.music.playbackcontrol.ACTION_SKIPPED"
    };

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        boolean isGoogle = Arrays.asList(ACTIONS_GOOGLEPLAY).contains(action);

        String artist, album, track;
        if(isGoogle){
            artist = intent.getStringExtra("artist");
            album = intent.getStringExtra("album");
            track = intent.getStringExtra("track");
        }else{
            artist = intent.getStringExtra("ARTIST_NAME");
            album = intent.getStringExtra("ALBUM_NAME");
            track = intent.getStringExtra("TRACK_NAME");
        }

        Music music = new Music(artist, album, track);
        ((App)context.getApplicationContext()).setMusic(music);
    }

}
