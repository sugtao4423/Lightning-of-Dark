package com.tao.lightning_of_dark;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import twitter4j.Status;
import twitter4j.TwitterException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

public class IntentActivity extends Activity {
	
	private ApplicationClass appClass;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent i;
		String uri;
		
		appClass = (ApplicationClass)this.getApplicationContext();
		
		if(appClass.getTwitter() == null)
			new MainActivity().LogIn(true, getApplicationContext());

		if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
			uri = getIntent().getData().toString();
			Matcher user = Pattern.compile("http(s)?://twitter.com/(\\w*)").matcher(uri);
			Matcher status = Pattern.compile("http(s)?://twitter.com/\\w*/status/(\\d*)").matcher(uri);
			if(status.find()){
				showStatus(Long.parseLong(status.group(2)));
			}else if(user.find()){
				i = new Intent(this, UserPage.class);
				i.putExtra("userScreenName", user.group(2));
				startActivity(i);
				finish();
			}
		}else if(Intent.ACTION_SEND.equals(getIntent().getAction())){
			uri = getIntent().getExtras().getCharSequence(Intent.EXTRA_TEXT).toString();
			i = new Intent(this, TweetActivity.class);
			i.putExtra("pakuri", uri);
			i.putExtra("do_back", false);
			startActivity(i);
			finish();
		}
	}
	public void showStatus(long tweetId){
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
		        	CustomAdapter adapter = new CustomAdapter(IntentActivity.this);
		        	adapter.add(status);
		        	ListView l = new ListView(IntentActivity.this);
			        l.setAdapter(adapter);
			        l.setOnItemClickListener(new ListViewListener(false));
			        l.setOnItemLongClickListener(new ListViewListener(false));
		        	AlertDialog.Builder builder = new AlertDialog.Builder(IntentActivity.this);
		        	builder.setView(l);
		        	builder.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							IntentActivity.this.finish();
						}
					});
		        	builder.create().show();
				}else
					new ShowToast("ツイートの取得に失敗しました", IntentActivity.this, 0);
			}
		};
		task.execute(tweetId);
	}
}
