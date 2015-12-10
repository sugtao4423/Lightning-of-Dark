package com.tao.lightning_of_dark;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ogwork.gesturetransformableview.view.GestureTransformableImageView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

public class Show_Image extends Activity{

	private byte[] non_orig_image;

	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_image);
		final GestureTransformableImageView image = (GestureTransformableImageView)findViewById(R.id.show_image_image);
		url = getIntent().getStringExtra("URL");

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
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					while(true){
						int len = input.read(buffer);
						if(len < 0)
							break;
						bout.write(buffer, 0, len);
					}
					non_orig_image = bout.toByteArray();
					Bitmap myBitmap = BitmapFactory.decodeByteArray(non_orig_image, 0, non_orig_image.length);
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
	
	public void image_option_click(View v){
		AlertDialog.Builder select = new AlertDialog.Builder(this);
		select.setItems(new String[]{"ブラウザで開く", "保存する"}, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				if(which == 0)
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				if(which == 1)
					saveImage();
			}
		});
		select.create().show();
	}

	public void saveImage(){
		final Matcher original = Pattern.compile("http(s)?://pbs.twimg.com/media/(.+)(\\..+):orig$").matcher(url + ":orig");
		if(!original.find()) {
			Matcher mm = Pattern.compile(".+/(.+)(\\..+)$").matcher(url);
			if(!mm.find()){
				Toast.makeText(this, "URLがパターンにマッチしません", Toast.LENGTH_SHORT).show();
				return;
			}
			save(mm.group(1), mm.group(2), non_orig_image, false);
			return;
		}

		AsyncTask<String, Void, byte[]> task = new AsyncTask<String, Void, byte[]>(){
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
			protected byte[] doInBackground(String... params){
				try{
					URL url = new URL(params[0]);
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					connection.setDoInput(true);
					connection.connect();
					InputStream input = connection.getInputStream();
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					while(true){
						int len = input.read(buffer);
						if(len < 0)
							break;
						bout.write(buffer, 0, len);
					}
					input.close();
					return bout.toByteArray();
				}catch(IOException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(final byte[] result){
				if(result != null) {
					progDailog.dismiss();
					save(original.group(2), original.group(3), result, true);
				}else{
					new ShowToast("オリジナル画像の取得に失敗しました", Show_Image.this, 0);
					finish();
				}
			}
		};
		task.execute(url + ":orig");
	}
	
	public void save(String fileName, String type, final byte[] byteImage, final boolean isOriginal){
		final String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;
		final String imgPath = saveDir + "/" + fileName + type;

		if(new File(imgPath).exists()) {
			final String newPath;
			String title;
			int i = 2;
			while(true){
				if(new File(saveDir + "/" + fileName +  "_" + i + type).exists()){
					i++;
				}else{
					newPath = saveDir + "/" + fileName + "_" + i + type;
					if(i == 2)
						title = fileName + type + "という名前のファイルが既に存在しています";
					else
						title = fileName + "_" + (i - 1) + type + "という名前のファイルが既に存在しています";
					break;
				}
			}
			AlertDialog.Builder exists = new AlertDialog.Builder(Show_Image.this);
			exists.setTitle(title)
			.setItems(new String[]{"上書き", "_" + i + "をつけて保存", "キャンセル"}, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					if(which == 0)
						output(imgPath, byteImage, isOriginal);
					if(which == 1)
						output(newPath, byteImage, isOriginal);
				}
			});
			exists.create().show();
		}else{
			output(imgPath, byteImage, isOriginal);
		}
	}

	public void output(String imgPath, byte[] byteImage, boolean isOriginal){
		FileOutputStream fos;
		try{
			fos = new FileOutputStream(imgPath, true);
			fos.write(byteImage);
			fos.close();
		}catch(IOException e){
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}
		if(isOriginal)
			Toast.makeText(this, "オリジナルを保存しました\n" + imgPath, Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "保存しました\n" + imgPath, Toast.LENGTH_LONG).show();
	}
}