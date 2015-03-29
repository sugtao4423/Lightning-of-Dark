package com.tao.lightning_of_dark;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class startOAuth extends Activity {
	
	static EditText pin, CustomCK, CustomCS;
	static String CK, CS;
	static SharedPreferences pref;
	
	static Twitter twitter;
	static TwitterFactory twitterFactory;
	static RequestToken rt;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oauth);
		
		pin = (EditText)findViewById(R.id.editText1);
		CustomCK = (EditText)findViewById(R.id.editText2);
		CustomCS = (EditText)findViewById(R.id.editText3);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		CustomCK.setText(pref.getString("CustomCK", ""));
		CustomCS.setText(pref.getString("CustomCS", ""));
	}
	
	public void ninsyo(View v){
		if(CustomCK.getText().toString().equals("")){
			CK = getString(R.string.CK);
			CS = getString(R.string.CS);
		}else{
			CK = CustomCK.getText().toString();
			CS = CustomCS.getText().toString();
			pref.edit().putString("CustomCK", CK).putString("CustomCS", CS).commit();
		}
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CK).setOAuthConsumerSecret(CS);
				Configuration jconf = builder.build();
				
				twitterFactory = new TwitterFactory(jconf);
				twitter = twitterFactory.getInstance();
				try{
					rt = twitter.getOAuthRequestToken();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rt.getAuthenticationURL())));
				}catch(Exception e){
					showToast("RequestTokenの取得に失敗しました");
				}
				return null;
			}
		};
		task.execute();
	}
	
	public void pin(View v){
		AsyncTask<Void, Void, AccessToken> task = new AsyncTask<Void, Void, AccessToken>(){

			@Override
			protected AccessToken doInBackground(Void... params) {
				try{
					AccessToken accessToken = twitter.getOAuthAccessToken(rt, pin.getText().toString());
					return accessToken;
				}catch(Exception e){
					showToast(e.toString());
				}
				return null;
			}
			protected void onPostExecute(AccessToken accessToken) {
				pref.edit().putString("AccessToken", accessToken.getToken())
				.putString("AccessTokenSecret", accessToken.getTokenSecret()).commit();
				startActivity(new Intent(getApplicationContext(), MainActivity.class));
				finish();
			}
		};
		task.execute();
	}
	
	public void showToast(String toast){
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
	}
}
