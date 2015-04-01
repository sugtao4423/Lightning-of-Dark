package com.tao.lightning_of_dark;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends FragmentActivity {
	
	static SharedPreferences pref;
	public static String CK, CS, MyScreenName; //MyScreenNameには「＠」は含まれない
	
	public static Twitter twitter;
	static TwitterFactory twitterFactory;
	static TwitterStream twitterStream;
	
	static AccessToken accessToken;
	static Configuration jconf;
	
	static CustomAdapter HomeAdapter, MentionAdapter;
	ResponseList<twitter4j.Status> home, mention;
	int HOME = 1, MENTION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(new MyFragmentStatePagerAdapter(getSupportFragmentManager()));
		viewPager.setCurrentItem(1);
		getActionBar().hide();
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
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
			LogIn();
		}
	}
	
	public void LogIn(){
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(CK).setOAuthConsumerSecret(CS);
				jconf = builder.build();
				
				twitterFactory = new TwitterFactory(jconf);
				twitter = twitterFactory.getInstance(accessToken);
				try{
					MyScreenName = twitter.getScreenName();
				}catch(Exception e){
					return false;
				}
				return true;
			}
			protected void onPostExecute(Boolean result) {
				if(result){
					getTimeLine();
					connectStreaming();
				}else
					showToast("スクリーンネームの取得に失敗しました", null);
			}
		};
		task.execute();
	}
	
	public void getTimeLine(){
		HomeAdapter = new CustomAdapter(this);
		MentionAdapter = new CustomAdapter(this);
		
		getHOME(); getMENTION();
		
		ListView foot = new ListView(this);
		foot.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				getHOME();
			}
		});
		Fragment_home.home.addFooterView(foot);
		
		ListView foot2 = new ListView(this);
		foot2.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				getMENTION();
			}
		});
		Fragment_mention.mention.addFooterView(foot2);
	}
	
	public void getHOME(){
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
					home = twitter.getHomeTimeline(new Paging(HOME, 50));
					return true;
				}catch(Exception e){
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					for(twitter4j.Status status : home)
						HomeAdapter.add(status);
					if(HOME == 1)
						new Fragment_home().setHome(HomeAdapter);
					HOME++;
				}else
					showToast("タイムライン取得エラー", null);
			}
		};
		task.execute();
	}
	public void getMENTION(){
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
					mention = twitter.getMentionsTimeline(new Paging(MENTION, 50));
					return true;
				}catch(Exception e){
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					for(twitter4j.Status status : mention)
						MentionAdapter.add(status);
					if(MENTION == 1)
						new Fragment_mention().setMention(MentionAdapter);
					MENTION++;
				}else
					showToast("タイムライン取得エラー", null);
			}
		};
		task.execute();
	}
	
	public void connectStreaming(){
		try{
			
			TwitterStreamFactory streamFactory = new TwitterStreamFactory(jconf);
			twitterStream = streamFactory.getInstance(accessToken);
			//UserStreamAdapter
			UserStreamAdapter streamAdapter = new UserStreamAdapter(){
				public void onStatus(final Status status){
					AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
						@Override
						protected Boolean doInBackground(Void... params) {
							return true;
						}
						protected void onPostExecute(Boolean result){
							HomeAdapter.insert(status, 0);
							if(status.getText().matches(".*@" + MyScreenName + ".*") || status.getText().startsWith("@" + MyScreenName))
								if(!status.isRetweet())
									MentionAdapter.insert(status, 0);
						}
					};
					task.execute();
				}
			};
			//ここまで
			twitterStream.addListener(streamAdapter);
			twitterStream.user();
			
		}catch(Exception e){
			showToast("ストリーミング系のエラー\n" + e.toString(), null);
		}
	}
	
	public void new_tweet(View v){
		Intent intent = new Intent(MainActivity.this, TweetActivity.class);
		startActivity(intent);
	}
	
	public void showToast(String toast, Context context){
		if(context == null)
			Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
	}
	
	public void onDestroy(){
		super.onDestroy();
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				if(twitterStream != null)
					twitterStream.shutdown();
				return null;
			}
		};
		task.execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
}
