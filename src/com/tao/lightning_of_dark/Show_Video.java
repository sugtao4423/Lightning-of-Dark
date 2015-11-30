package com.tao.lightning_of_dark;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class Show_Video extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_video);

		String url = getIntent().getStringExtra("URL");
		final ApplicationClass appClass = (ApplicationClass)getApplicationContext();

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
				if(appClass.getIsVideoLoop()) {
					vv.seekTo(0);
					vv.start();
				}else{
					finish();
				}
			}
		});
	}
}