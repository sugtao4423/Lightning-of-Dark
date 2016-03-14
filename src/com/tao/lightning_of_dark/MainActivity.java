package com.tao.lightning_of_dark;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import MainFragment.Fragment_home;
import MainFragment.Fragment_mention;
import MainFragment.MyFragmentStatePagerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends FragmentActivity{

	private String myScreenName; // MyScreenNameには「＠」は含まれない

	private Twitter twitter;
	private TwitterStream twitterStream;
	private Pattern mentionPattern;

	private ApplicationClass appClass;

	private SharedPreferences pref;
	private boolean resetFlag;

	private AccessToken accessToken;
	private Configuration conf;

	private CustomAdapter[] listAdapters;
	
	private Fragment_mention fragmentMention;
	private Fragment_home fragmentHome;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_main);

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		MyFragmentStatePagerAdapter pagerAdapter = new MyFragmentStatePagerAdapter(getSupportFragmentManager(), this);
		
		ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(1);
		viewPager.setOffscreenPageLimit(pref.getInt("SelectListCount", 0) + 1);
		listAdapters = new CustomAdapter[pref.getInt("SelectListCount", 0)];
		for(int i = 0; i < pref.getInt("SelectListCount", 0); i++)
			listAdapters[i] = new CustomAdapter(this);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.mainPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		fragmentHome = pagerAdapter.getFragmentHome();
		fragmentMention = pagerAdapter.getFragmentMention();
		logIn();
	}

	@SuppressLint("InflateParams")
	public void logIn(){
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(String... params){
				conf = new ConfigurationBuilder().setOAuthConsumerKey(params[0]).setOAuthConsumerSecret(params[1]).build();

				twitter = new TwitterFactory(conf).getInstance(accessToken);
				try{
					myScreenName = twitter.getScreenName();
				}catch(Exception e){
					return false;
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(result) {
					appClass.setMyScreenName(myScreenName);
					appClass.setTwitter(twitter);
					mentionPattern = Pattern.compile(".*@" + myScreenName + ".*", Pattern.DOTALL);
					appClass.setMentionPattern(mentionPattern);
					getTimeLine();
					connectStreaming();
				}else
					new ShowToast("スクリーンネームの取得に失敗しました", MainActivity.this, 0);
			}
		};
		appClass = (ApplicationClass)getApplicationContext();
		appClass.setListAdapters(listAdapters);
		boolean[] list_alreadyLoad = new boolean[listAdapters.length];
		for(int i = 0; i < listAdapters.length; i++)
			list_alreadyLoad[i] = false;
		appClass.setList_AlreadyLoad(list_alreadyLoad);
		appClass.loadOption(this);

		View customToast = LayoutInflater.from(this).inflate(R.layout.custom_toast, null);
		appClass.setToastView(customToast);
		appClass.setToast_Main_Message((TextView)customToast.findViewById(R.id.toast_main_message));
		appClass.setToast_Tweet((TextView)customToast.findViewById(R.id.toast_tweet));
		appClass.setToast_Icon((SmartImageView)customToast.findViewById(R.id.toast_icon));

		if(pref.getString("AccessToken", "").equals("")) {
			startActivity(new Intent(this, StartOAuth.class));
			finish();
		}else{
			String ck, cs;
			if(pref.getString("CustomCK", "").equals("")) {
				ck = getString(R.string.CK);
				cs = getString(R.string.CS);
			}else{
				ck = pref.getString("CustomCK", null);
				cs = pref.getString("CustomCS", null);
			}
			accessToken = new AccessToken(pref.getString("AccessToken", ""), pref.getString("AccessTokenSecret", ""));
			task.execute(ck, cs);
		}
	}

	public void getTimeLine(){
		new AsyncTask<Void, Void, Boolean>(){
			
			private ResponseList<twitter4j.Status> home;
			private ResponseList<twitter4j.Status> mention;
			
			@Override
			protected Boolean doInBackground(Void... params){
				try{
					home = twitter.getHomeTimeline(new Paging(1, 50));
					mention = twitter.getMentionsTimeline(new Paging(1, 50));
					return true;
				}catch(Exception e){
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(result) {
					for(twitter4j.Status status : home)
						fragmentHome.add(status);
					for(twitter4j.Status status : mention)
						fragmentMention.add(status);
				}else{
					new ShowToast("タイムライン取得エラー", MainActivity.this, 0);
				}
			}
		}.execute();

		if(!pref.getString("startApp_loadLists", "").equals("") && !pref.getString("SelectListIds", "").equals("")) {
			String[] listName_str = pref.getString("SelectListNames", "").split(",", 0);
			String[] listIds_str = pref.getString("SelectListIds", "").split(",", 0);
			String[] startApp_loadLists = pref.getString("startApp_loadLists", "").split(",", 0);
			ArrayList<String> startApp_loadListsArray = new ArrayList<String>();
			for(int i = 0; i < startApp_loadLists.length; i++)
				startApp_loadListsArray.add(startApp_loadLists[i]);
			long[] listIds = new long[listIds_str.length];
			for(int i = 0; i < listIds_str.length; i++)
				listIds[i] = Long.parseLong(listIds_str[i]);

			for(int i = 0; i < listIds.length; i++){
				if(startApp_loadListsArray.indexOf(listName_str[i]) != -1)
					getList(listIds[i], i);
			}
		}
	}

	public void getList(final long listId, final int index){
		new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params){
				try{
					return twitter.getUserListStatuses(listId, new Paging(1, 50));
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result != null) {
					for(twitter4j.Status status : result)
						listAdapters[index].add(status);
					boolean[] tmp = appClass.getList_AlreadyLoad();
					tmp[index] = true;
					appClass.setList_AlreadyLoad(tmp);
				}
			}
		}.execute();
	}

	public void connectStreaming(){
		try{
			twitterStream = new TwitterStreamFactory(conf).getInstance(accessToken);

			// UserStreamAdapter
			UserStreamAdapter streamAdapter = new UserStreamAdapter(){
				@Override
				public void onStatus(final Status status){
					fragmentHome.insert(status);
					if(mentionPattern.matcher(status.getText()).find() && !status.isRetweet())
						fragmentMention.insert(status);
				}
			};
			// ConnectionLifeCycleListener
			ConnectionLifeCycleListener clcl = new ConnectionLifeCycleListener(){
				@Override
				public void onDisconnect(){
					toast("接続が切れました");
				}

				@Override
				public void onConnect(){
					toast("接続しました");
				}

				@Override
				public void onCleanUp(){
				}

				public void toast(final String text){
					MainActivity.this.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							new ShowToast(text, MainActivity.this, 0);
						}
					});
				}
			};
			twitterStream.addListener(streamAdapter);
			twitterStream.addConnectionLifeCycleListener(clcl);
			twitterStream.user();
		}catch(Exception e){
			new ShowToast("ストリーミング系のエラー\n" + e.toString(), MainActivity.this, 0);
		}
	}

	public void new_tweet(View v){
		Intent intent = new Intent(MainActivity.this, TweetActivity.class);
		startActivity(intent);
	}

	public void option(View v){
		final String[] items = new String[]{"ツイート爆撃", "ユーザー検索", "アカウント", "設定"};
		new AlertDialog.Builder(this)
		.setItems(items, new OptionClickListener(this, items, myScreenName, pref, twitter))
		.create().show();
	}

	public void restart(){
		resetFlag = true;
		finish();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(resetFlag) {
			resetFlag = false;
			startActivity(new Intent(this, MainActivity.class));
		}else{
			new AsyncTask<Void, Void, Void>(){
				@Override
				protected Void doInBackground(Void... params){
					if(twitterStream != null)
						twitterStream.shutdown();
					return null;
				}
			}.execute();
		}
	}
}