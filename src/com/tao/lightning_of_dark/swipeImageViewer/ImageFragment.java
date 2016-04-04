package com.tao.lightning_of_dark.swipeImageViewer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ImageFragment extends Fragment{

	private ZoomableImageView image;

	private String url;
	private byte[] non_orig_image;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		url = bundle.getString("url");
		image = new ZoomableImageView(getActivity());
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		new AsyncTask<String, Void, Bitmap>(){
			private ProgressDialog progDailog;

			@Override
			protected void onPreExecute(){
				progDailog = new ProgressDialog(getActivity());
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
					Toast.makeText(getActivity(), "画像の取得失敗", Toast.LENGTH_LONG).show();
				}
			}
		}.execute(url);

		return image;
	}
}
