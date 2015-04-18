package com.tao.lightning_of_dark;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class startOAuth extends Activity {
	
	EditText pin, CustomCK, CustomCS;
	Button ninsyobtn;
	String CK, CS;
	SharedPreferences pref;
	
	Twitter twitter;
	TwitterFactory twitterFactory;
	RequestToken rt;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oauth);
		
		TextView description = (TextView)findViewById(R.id.OAuthDescription);
		String descri = "Custom CK/CSを使う場合、CallbackURLを<br><font color=blue><u>https://twitter.com/lightning-of-dark</u></font><br>に設定してください。<br>（タップでコピー）";
		description.setText(Html.fromHtml(descri));
		
		CustomCK = (EditText)findViewById(R.id.editText2);
		CustomCS = (EditText)findViewById(R.id.editText3);
		
		ninsyobtn = (Button)findViewById(R.id.button1);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		CustomCK.setText(pref.getString("CustomCK", ""));
		CustomCS.setText(pref.getString("CustomCS", ""));
	}
	
	public void ninsyo(View v){
		ninsyobtn.setEnabled(false);
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
					rt = twitter.getOAuthRequestToken("lightning-of-dark://twitter");
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rt.getAuthenticationURL())));
				}catch(Exception e){
					new ShowToast("RequestTokenの取得に失敗しました", startOAuth.this);
				}
				return null;
			}
		};
		task.execute();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent == null
                || intent.getData() == null
                || !intent.getData().toString().startsWith("lightning-of-dark://twitter")) {
            return;
        }
		final String verifier = intent.getData().getQueryParameter("oauth_verifier");
		
		AsyncTask<Void, Void, AccessToken> task = new AsyncTask<Void, Void, AccessToken>(){

			@Override
			protected AccessToken doInBackground(Void... params) {
				try{
					AccessToken accessToken = twitter.getOAuthAccessToken(rt, verifier);
					return accessToken;
				}catch(Exception e){
					//失敗
				}
				return null;
			}
			protected void onPostExecute(AccessToken accessToken) {
				if(accessToken != null){
					pref.edit().putString("AccessToken", accessToken.getToken())
					.putString("AccessTokenSecret", accessToken.getTokenSecret()).commit();
					startActivity(new Intent(getApplicationContext(), MainActivity.class));
					finish();
				}else{
					new ShowToast("失敗...", startOAuth.this);
					finish();
				}
			}
		};
		task.execute();
	}
	
	public void Description(View v){
		ClipboardManager clipboardManager = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clipData = ClipData.newPlainText("Lightning of Dark", "https://twitter.com/lightning-of-dark");
		clipboardManager.setPrimaryClip(clipData);
		new ShowToast("クリップボードにコピーしました", this);
	}
}
