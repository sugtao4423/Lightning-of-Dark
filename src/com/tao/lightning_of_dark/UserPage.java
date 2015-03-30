package com.tao.lightning_of_dark;

import twitter4j.TwitterException;

import com.loopj.android.image.SmartImageView;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class UserPage extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userpage);
		
		setBanner();
	}
	
	@SuppressWarnings("static-access")
	public void setBanner(){
		final SmartImageView banner = (SmartImageView)findViewById(R.id.banner);
		
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>(){
			
			@Override
			protected String doInBackground(Void... params) {
				try {
					return new MainActivity().twitter.verifyCredentials().getProfileBannerURL();
				} catch (TwitterException e) {
				}
				return null;
			}
			protected void onPostExecute(String result) {
			    banner.setImageUrl(result);
			  }
		};
		task.execute();
	}

}
