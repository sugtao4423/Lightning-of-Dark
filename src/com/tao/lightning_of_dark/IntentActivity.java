package com.tao.lightning_of_dark;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.userPageFragment.UserPage;

import twitter4j.Status;
import twitter4j.TwitterException;
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

public class IntentActivity extends Activity{

	@SuppressLint("InflateParams")
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		ApplicationClass appClass = (ApplicationClass)getApplicationContext();

		if(appClass.getTwitter() == null){
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			appClass = (ApplicationClass)this.getApplicationContext();
			appClass.loadOption(this);

			View customToast = LayoutInflater.from(this).inflate(R.layout.custom_toast, null);
			appClass.setToastView(customToast);
			appClass.setToast_Main_Message((TextView)customToast.findViewById(R.id.toast_main_message));
			appClass.setToast_Tweet((TextView)customToast.findViewById(R.id.toast_tweet));
			appClass.setToast_Icon((SmartImageView)customToast.findViewById(R.id.toast_icon));

			if(pref.getString("AccessToken", "").equals("")){
				startActivity(new Intent(this, StartOAuth.class));
				finish();
			}else{
				appClass.twitterLogin(this);
				jump();
			}
		}else{
			jump();
		}
	}

	public void jump(){
		if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
			String uri = getIntent().getData().toString();
			Matcher user = Pattern.compile("http(s)?://twitter.com/([0-9a-zA-Z_]+)").matcher(uri);
			Matcher status = Pattern.compile("http(s)?://twitter.com/[0-9a-zA-Z_]+/status/([0-9]+)").matcher(uri);
			if(status.find()){
				showStatus(Long.parseLong(status.group(2)), IntentActivity.this, true);
			}else if(user.find()){
				Intent i = new Intent(IntentActivity.this, UserPage.class);
				i.putExtra("userScreenName", user.group(2));
				startActivity(i);
				finish();
			}
		}else if(Intent.ACTION_SEND.equals(getIntent().getAction())){
			String subject = getIntent().getExtras().getString(Intent.EXTRA_SUBJECT);
			String text = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
			text = subject.isEmpty() ? text : (subject + " " + text);
			Intent i = new Intent(IntentActivity.this, TweetActivity.class);
			i.putExtra("type", TweetActivity.TYPE_EXTERNALTEXT);
			i.putExtra("text", text);
			startActivity(i);
			finish();
		}
	}

	public void showStatus(long tweetId, final Context context, final boolean isClose){
		final ApplicationClass appClass = (ApplicationClass)context.getApplicationContext();
		new AsyncTask<Long, Void, Status>(){
			@Override
			protected twitter4j.Status doInBackground(Long... params){
				try{
					return appClass.getTwitter().showStatus(params[0]);
				}catch(TwitterException e){
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
					l.setOnItemClickListener(new ListViewListener());
					l.setOnItemLongClickListener(new ListViewListener());
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setView(l);
					if(isClose){
						builder.setOnDismissListener(new OnDismissListener(){
							@Override
							public void onDismiss(DialogInterface dialog){
								((Activity)context).finish();
							}
						});
					}
					builder.show();
				}else{
					new ShowToast("ツイートの取得に失敗しました", IntentActivity.this, 0);
				}
			}
		}.execute(tweetId);
	}
}