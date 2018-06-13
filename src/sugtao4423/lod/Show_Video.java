package sugtao4423.lod;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class Show_Video extends Activity{

	public static final String INTENT_EXTRA_KEY_TYPE = "type";
	public static final String INTENT_EXTRA_KEY_URL = "URL";

	public static final int TYPE_VIDEO = 0;
	public static final int TYPE_GIF = 1;

	private App app;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_video);
		app = (App)getApplicationContext();
		if(app.getOptions().getIsVideoOrientationSensor())
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);


		Intent intent = getIntent();
		String url = intent.getStringExtra(INTENT_EXTRA_KEY_URL);
		final int type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, -1);
		if(type == -1)
			finish();

		final VideoView vv = (VideoView)findViewById(R.id.tw_video);
		vv.setMediaController(new MediaController(this));

		final ProgressDialog progDialog = new ProgressDialog(Show_Video.this);
		progDialog.setMessage("Loading...");
		progDialog.setIndeterminate(false);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setCancelable(true);
		progDialog.show();

		vv.setVideoURI(Uri.parse(url));
		vv.setOnPreparedListener(new OnPreparedListener(){

			@Override
			public void onPrepared(MediaPlayer mp){
				progDialog.dismiss();
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

	@Override
	public void onResume(){
		super.onResume();
		app.getUseTime().start();
	}

	@Override
	public void onPause(){
		super.onPause();
		app.getUseTime().stop();
	}

}