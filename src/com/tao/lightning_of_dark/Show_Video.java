package com.tao.lightning_of_dark;

import com.tao.lightning_of_dark.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class Show_Video extends Activity{

	public static final int TYPE_VIDEO = 0;
	public static final int TYPE_GIF = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_video);

		Intent intent = getIntent();
		String url = intent.getStringExtra("URL");
		final int type = intent.getIntExtra("type", -1);
		if(type == -1)
			finish();

		final VideoView vv = (VideoView)findViewById(R.id.tw_video);
		vv.setMediaController(new MediaController(this));

		final ProgressDialog progDailog = new ProgressDialog(Show_Video.this);
		progDailog.setMessage("Loading...");
		progDailog.setIndeterminate(false);
		progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDailog.setCancelable(true);
		progDailog.show();

		vv.setVideoURI(Uri.parse(url));
		vv.setOnPreparedListener(new OnPreparedListener(){

			@Override
			public void onPrepared(MediaPlayer mp){
				progDailog.dismiss();
				vv.start();
			}
		});

		vv.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer mp){
				if(type == TYPE_GIF){
					vv.seekTo(0);
					vv.start();
				}else{
					finish();
				}
			}
		});
	}
}