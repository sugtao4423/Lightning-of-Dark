package com.tao.lightning_of_dark.swipeImageViewer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class ImageFragmentActivity extends FragmentActivity{

	private ViewPager pager;
	private String[] urls;
	private String currentUrl;
	private byte[] non_orig_image;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show_image_pager);
		Intent intent = getIntent();
		urls = intent.getStringArrayExtra("urls");
		int pos = intent.getIntExtra("position", 0);
		ImagePagerAdapter adapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);

		pager = (ViewPager)findViewById(R.id.show_image_pager);
		pager.setAdapter(adapter);
		pager.setOffscreenPageLimit(urls.length - 1);
		pager.setCurrentItem(pos);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.show_image_pager_tab_strip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
	}

	public void image_option_click(View v){
		currentUrl = urls[pager.getCurrentItem()];
		AlertDialog.Builder select = new AlertDialog.Builder(this);
		select.setItems(new String[]{"ブラウザで開く", "保存する"}, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				if(which == 0)
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)));
				if(which == 1)
					saveImage();
			}
		});
		select.create().show();
	}

	public void saveImage(){
		final Matcher original = Pattern.compile("http(s)?://pbs.twimg.com/media/(.+)(\\..+):orig$").matcher(currentUrl + ":orig");
		if(!original.find()) {
			Matcher mm = Pattern.compile(".+/(.+)(\\..+)$").matcher(currentUrl);
			if(!mm.find()) {
				new ShowToast("URLがパターンにマッチしません", this, 0);
				return;
			}
			save(mm.group(1), mm.group(2), non_orig_image, false);
			return;
		}

		new AsyncTask<String, Void, byte[]>(){
			private ProgressDialog progDailog;

			@Override
			protected void onPreExecute(){
				progDailog = new ProgressDialog(ImageFragmentActivity.this);
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
					new ShowToast("オリジナル画像の取得に失敗しました", ImageFragmentActivity.this, 0);
				}
			}
		}.execute(currentUrl + ":orig");
	}

	public void save(final String fileName, final String type, final byte[] byteImage, final boolean isOriginal){
		final String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;
		final String imgPath = saveDir + "/" + fileName + type;

		if(new File(imgPath).exists()) {
			new AlertDialog.Builder(this)
			.setTitle("エラー:ファイルが既に存在しています")
			.setItems(new String[]{"上書き", "ファイル名を指定して保存", "キャンセル"}, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					if(which == 0) {
						output(imgPath, byteImage, isOriginal);
					}else if(which == 1) {
						final EditText edit = new EditText(ImageFragmentActivity.this);
						edit.setText(fileName);
						new AlertDialog.Builder(ImageFragmentActivity.this)
						.setTitle("ファイル名を指定してください")
						.setView(edit)
						.setNegativeButton("キャンセル", null)
						.setPositiveButton("OK", new OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which){
								String newPath = saveDir + "/" + edit.getText().toString() + type;
								output(newPath, byteImage, isOriginal);
							}
						}).show();
					}
				}
			}).show();
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
