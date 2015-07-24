package com.tao.lightning_of_dark;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class IntentActivity extends Activity {
	
	private ApplicationClass appClass;
	
	private Intent i;
	private String uri;
	
	private String CK, CS;
	private AccessToken accessToken;
	private Twitter twitter;
	private String MyScreenName;
	private Pattern mentionPattern;
	
	private SharedPreferences pref;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		appClass = (ApplicationClass)getApplicationContext();
		
		if(appClass.getTwitter() == null){
			AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
				@Override
				protected Boolean doInBackground(Void... params) {
					Configuration conf = new ConfigurationBuilder()
					.setOAuthConsumerKey(CK).setOAuthConsumerSecret(CS).build();
					
					twitter = new TwitterFactory(conf).getInstance(accessToken);
					try{
						MyScreenName = twitter.getScreenName();
					}catch(Exception e){
						return false;
					}
					return true;
				}
				@Override
				protected void onPostExecute(Boolean result) {
					if(result){
						appClass.setMyScreenName(MyScreenName);
						appClass.setTwitter(twitter);
						mentionPattern = Pattern.compile(".*@" + MyScreenName + ".*", Pattern.DOTALL);
						appClass.setMentionPattern(mentionPattern);
						
						jump();						
					}else
						new ShowToast("スクリーンネームの取得に失敗しました", IntentActivity.this, 0);
				}
			};
			pref = PreferenceManager.getDefaultSharedPreferences(this);
			appClass = (ApplicationClass)this.getApplicationContext();
			appClass.loadOption(this);
			
			View customToast = LayoutInflater.from(this).inflate(R.layout.custom_toast, null);
			appClass.setToastView(customToast);
			appClass.setToast_Main_Message((TextView)customToast.findViewById(R.id.toast_main_message));
			appClass.setToast_Tweet((TextView)customToast.findViewById(R.id.toast_tweet));
			appClass.setToast_Icon((SmartImageView)customToast.findViewById(R.id.toast_icon));
			
			if(pref.getString("AccessToken", "").equals("")){
				startActivity(new Intent(this, startOAuth.class));
				finish();
			}else{
				if(pref.getString("CustomCK", "").equals("")){
					CK = getString(R.string.CK);
					CS = getString(R.string.CS);
				}else{
					CK = pref.getString("CustomCK", null);
					CS = pref.getString("CustomCS", null);
				}
				accessToken = new AccessToken(pref.getString("AccessToken", ""), pref.getString("AccessTokenSecret", ""));
				task.execute();
			}
		}else{
			jump();
		}
	}
	
	public void jump(){
		if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
			uri = getIntent().getData().toString();
			Matcher user = Pattern.compile("http(s)?://twitter.com/([0-9a-zA-Z_]+)").matcher(uri);
			Matcher status = Pattern.compile("http(s)?://twitter.com/[0-9a-zA-Z_]+/status/(\\d+)").matcher(uri);
			if(status.find()){
				showStatus(Long.parseLong(status.group(2)), IntentActivity.this, true);
			}else if(user.find()){
				i = new Intent(IntentActivity.this, UserPage.class);
				i.putExtra("userScreenName", user.group(2));
				startActivity(i);
				finish();
			}
		}else if(Intent.ACTION_SEND.equals(getIntent().getAction())){
			uri = getIntent().getExtras().getCharSequence(Intent.EXTRA_TEXT).toString();
			i = new Intent(IntentActivity.this, TweetActivity.class);
			i.putExtra("pakuri", uri);
			i.putExtra("do_back", false);
			startActivity(i);
			finish();
		}
	}
	
	public void showStatus(long tweetId, final Context context, final boolean isClose){
		appClass = (ApplicationClass)context.getApplicationContext();
		AsyncTask<Long, Void, Status> task = new AsyncTask<Long, Void, Status>(){
			@Override
			protected twitter4j.Status doInBackground(Long... params) {
				try {
					return appClass.getTwitter().showStatus(params[0]);
				} catch (TwitterException e) {
					return null;
				}
			}
			@SuppressLint("NewApi")
			@Override
			protected void onPostExecute(twitter4j.Status status){
				if(status != null){
		        	CustomAdapter adapter = new CustomAdapter(context);
		        	adapter.add(status);
		        	ListView l = new ListView(context);
			        l.setAdapter(adapter);
			        l.setOnItemClickListener(new ListViewListener(false));
			        l.setOnItemLongClickListener(new ListViewListener(false));
		        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
		        	builder.setView(l);
		        	if(isClose){
			        	builder.setOnDismissListener(new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								((Activity)context).finish();
							}
						});
		        	}
		        	builder.create().show();
				}else
					new ShowToast("ツイートの取得に失敗しました", IntentActivity.this, 0);
			}
		};
		task.execute(tweetId);
	}
}