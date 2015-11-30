package com.tao.lightning_of_dark;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.ogwork.gesturetransformableview.view.GestureTransformableImageView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

public class Show_Image extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		final String url = getIntent().getStringExtra("URL");

		final GestureTransformableImageView image = new GestureTransformableImageView(this,
				GestureTransformableImageView.GESTURE_DRAGGABLE | GestureTransformableImageView.GESTURE_SCALABLE);
		image.setLimitScaleMin(1F);
		image.setLimitScaleMax(3.5F);
		setContentView(image);

		AsyncTask<String, Void, Bitmap> task = new AsyncTask<String, Void, Bitmap>(){
			private ProgressDialog progDailog;

			@Override
			protected void onPreExecute(){
				progDailog = new ProgressDialog(Show_Image.this);
				progDailog.setMessage("Loading...");
				progDailog.setIndeterminate(false);
				progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDailog.setCancelable(true);
				progDailog.show();
			}

			@Override
			protected Bitmap doInBackground(String... params){
				try{
					URL url = new URL(params[0]);
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					connection.setDoInput(true);
					connection.connect();
					InputStream input = connection.getInputStream();
					Bitmap myBitmap = BitmapFactory.decodeStream(input);
					input.close();
					return myBitmap;
				}catch(IOException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Bitmap result){
				if(result != null) {
					progDailog.dismiss();
					image.setImageBitmap(result);
				}else{
					new ShowToast("画像の取得に失敗しました", Show_Image.this, 0);
					finish();
				}
			}
		};
		task.execute(url);
	}
}