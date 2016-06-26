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
import com.tenthbit.view.ZoomViewPager;

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
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class ImageFragmentActivity extends FragmentActivity{

	public static final int TYPE_ICON = 0;
	public static final int TYPE_BANNER = 1;

	private ImagePagerAdapter adapter;
	private ZoomViewPager pager;
	private String[] urls;
	private int type;
	private String currentUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show_image_pager);
		Intent intent = getIntent();
		urls = intent.getStringArrayExtra("urls");
		type = intent.getIntExtra("type", -1);
		int pos = intent.getIntExtra("position", 0);
		adapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);

		pager = (ZoomViewPager)findViewById(R.id.show_image_pager);
		pager.setAdapter(adapter);
		pager.setOffscreenPageLimit(urls.length - 1);
		pager.setCurrentItem(pos);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.show_image_pager_tab_strip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
	}

	public void image_option_click(View v){
		currentUrl = urls[pager.getCurrentItem()];
		new AlertDialog.Builder(this)
		.setItems(new String[]{"ブラウザで開く", "保存する"}, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				if(which == 0)
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)));
				if(which == 1)
					saveImage();
			}
		}).show();
	}

	public void saveImage(){
		if(type == TYPE_BANNER) {
			Matcher banner = Pattern.compile("^http(s)?://pbs.twimg.com/profile_banners/[0-9]+/([0-9]+)/web_retina$").matcher(currentUrl);
			if(!banner.find()) {
				new ShowToast("URLがパターンにマッチしません\n保存できませんでした", this, 0);
				return;
			}
			byte[] non_orig_image = ((ImageFragment)adapter.getItem(pager.getCurrentItem())).getNonOrigImage();
			save(banner.group(2), ".jpg", non_orig_image, false);
			return;
		}

		String orig = "";
		if(type != TYPE_ICON)
			orig = ":orig";

		final Matcher pattern = Pattern.compile("^http(s)?://pbs.twimg.com/.+/+(.+)(\\..+)" + orig + "$").matcher(currentUrl + orig);
		if(!pattern.find()) {
			new ShowToast("URLがパターンにマッチしません\n保存できませんでした", this, 0);
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
					if(type == TYPE_ICON)
						save(pattern.group(2), pattern.group(3), result, false);
					else
						save(pattern.group(2), pattern.group(3), result, true);
				}else{
					new ShowToast("オリジナル画像の取得に失敗しました", ImageFragmentActivity.this, 0);
				}
			}
		}.execute(currentUrl + orig);
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
								if(new File(newPath).exists())
									save(fileName, type, byteImage, isOriginal);
								else
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
