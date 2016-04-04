package com.tao.lightning_of_dark;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.dataclass.Account;
import com.tao.lightning_of_dark.utils.DBUtil;

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

public class StartOAuth extends Activity{

	private EditText customCK, customCS;
	private Button ninsyobtn;
	private String ck, cs;
	private SharedPreferences pref;

	private Twitter twitter;
	private TwitterFactory twitterFactory;
	private RequestToken rt;

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oauth);

		TextView description = (TextView)findViewById(R.id.OAuthDescription);
		String descri = "Custom CK/CSを使う場合、CallbackURLを<br><font color=blue><u>https://twitter.com/lightning-of-dark</u></font><br>に設定してください。<br>（タップでコピー）";
		description.setText(Html.fromHtml(descri));

		customCK = (EditText)findViewById(R.id.editText2);
		customCS = (EditText)findViewById(R.id.editText3);

		ninsyobtn = (Button)findViewById(R.id.ninsyo);

		pref = PreferenceManager.getDefaultSharedPreferences(this);

		customCK.setText(pref.getString("CustomCK", ""));
		customCS.setText(pref.getString("CustomCS", ""));
	}

	public void ninsyo(View v){
		ninsyobtn.setEnabled(false);
		if(customCK.getText().toString().equals("")) {
			ck = getString(R.string.CK);
			cs = getString(R.string.CS);
		}else{
			ck = customCK.getText().toString();
			cs = customCS.getText().toString();
			pref.edit().putString("CustomCK", ck).putString("CustomCS", cs).commit();
		}

		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params){
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(ck).setOAuthConsumerSecret(cs);
				Configuration jconf = builder.build();

				twitterFactory = new TwitterFactory(jconf);
				twitter = twitterFactory.getInstance();
				try{
					rt = twitter.getOAuthRequestToken("lightning-of-dark://twitter");
					return true;
				}catch(Exception e){
					return false;
				}
			}

			@Override
			public void onPostExecute(Boolean result){
				if(result)
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rt.getAuthenticationURL())));
				else
					new ShowToast("RequestTokenの取得に失敗しました", StartOAuth.this, 0);
			}
		}.execute();
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		if(intent == null || intent.getData() == null || !intent.getData().toString().startsWith("lightning-of-dark://twitter")) {
			return;
		}
		final String verifier = intent.getData().getQueryParameter("oauth_verifier");

		new AsyncTask<Void, Void, AccessToken>(){
			@Override
			protected AccessToken doInBackground(Void... params){
				try{
					return twitter.getOAuthAccessToken(rt, verifier);
				}catch(Exception e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(AccessToken accessToken){
				if(accessToken != null) {
					pref.edit().putString("AccessToken", accessToken.getToken())
							.putString("AccessTokenSecret", accessToken.getTokenSecret()).commit();

					if(ck.equals(getString(R.string.CK)))
						ck = "";
					if(cs.equals(getString(R.string.CS)))
						cs = "";

					Account account = new Account(accessToken.getScreenName(), ck, cs,
							accessToken.getToken(), accessToken.getTokenSecret(), false, 0, "-1", "", "");
					new DBUtil(StartOAuth.this).addAcount(account);

					new ShowToast("アカウントを追加しました", StartOAuth.this, 0);
					startActivity(new Intent(getApplicationContext(), MainActivity.class));
				}else{
					new ShowToast("失敗...", StartOAuth.this, 0);
				}
				finish();
			}
		}.execute();
	}

	public void Description(View v){
		ClipboardManager clipboardManager = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clipData = ClipData.newPlainText("Lightning of Dark", "https://twitter.com/lightning-of-dark");
		clipboardManager.setPrimaryClip(clipData);
		new ShowToast("クリップボードにコピーしました", this, 0);
	}
}
