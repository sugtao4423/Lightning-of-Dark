package com.tao.lightning_of_dark;

import java.util.regex.Pattern;

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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
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
	
	ViewPager viewPager;
	
	static CustomAdapter HomeAdapter, MentionAdapter;
	ResponseList<twitter4j.Status> home, mention;
	static Pattern mentionPattern;
	
	static boolean loadOptionPref, menu_reply, menu_retweet, menu_fav, menu_regex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_main);
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(new MyFragmentStatePagerAdapter(getSupportFragmentManager()));
		viewPager.setCurrentItem(1);
		
		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.mainPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		menu_reply = pref.getBoolean("menu_reply", true);
		menu_retweet = pref.getBoolean("menu_retweet", true);
		menu_fav = pref.getBoolean("menu_fav", true);
		menu_regex = pref.getBoolean("menu_regex", false);
		
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
					mentionPattern = Pattern.compile(".*@" + MainActivity.MyScreenName + ".*", Pattern.DOTALL);
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
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
					home = twitter.getHomeTimeline(new Paging(1, 50));
					mention = twitter.getMentionsTimeline(new Paging(1, 50));
					return true;
				}catch(Exception e){
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					for(twitter4j.Status status : home)
						HomeAdapter.add(status);
					for(twitter4j.Status status : mention)
						MentionAdapter.add(status);
					new Fragment_home().setHome(HomeAdapter);
					new Fragment_mention().setMention(MentionAdapter);
				}else
					showToast("タイムライン取得エラー", null);
			}
		};
		task.execute();
		
		MoreHome(); MoreMention();
	}
	
	public void MoreHome(){
		ListView foot = new ListView(this);
		foot.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Status s = (Status)Fragment_home.home.getItemAtPosition(Fragment_home.home.getAdapter().getCount() - 2);
				final long tweetId = s.getId();
				AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
					@Override
					protected Boolean doInBackground(Void... params) {
						try{
							home = twitter.getHomeTimeline(new Paging(1, 50).maxId(tweetId - 1));
							return true;
						}catch(Exception e){
							return false;
						}
					}
					protected void onPostExecute(Boolean result){
						if(result){
							for(twitter4j.Status status : home)
									HomeAdapter.add(status);
						}else
							showToast("タイムライン取得エラー", null);
					}
				};
				task.execute();
			}
		});
		Fragment_home.home.addFooterView(foot);
	}
	public void MoreMention(){
		ListView foot = new ListView(this);
		foot.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Status s = (Status)Fragment_mention.mention.getItemAtPosition(Fragment_mention.mention.getAdapter().getCount() - 2);
				final long tweetId = s.getId();
				AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
					@Override
					protected Boolean doInBackground(Void... params) {
						try{
							mention = twitter.getMentionsTimeline(new Paging(1, 50).maxId(tweetId - 1));
							return true;
						}catch(Exception e){
							return false;
						}
					}
					protected void onPostExecute(Boolean result){
						if(result){
							for(twitter4j.Status status : mention)
									MentionAdapter.add(status);
						}else
							showToast("メンション取得エラー", null);
					}
				};
				task.execute();
			}
		});
		Fragment_mention.mention.addFooterView(foot);
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
							if(mentionPattern.matcher(status.getText()).find() && !status.isRetweet())
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
			Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
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
