package com.tao.lightning_of_dark;

import java.util.ArrayList;
import java.util.List;
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
import MainFragment.MyFragmentStatePagerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	private String CK, CS, MyScreenName; //MyScreenNameには「＠」は含まれない
	
	private Twitter twitter;
	private Pattern mentionPattern;
	
	private ApplicationClass appClass;
	
	private SharedPreferences pref;
	private boolean resetFlag;
	
	private TwitterFactory twitterFactory;
	private TwitterStream twitterStream;
	
	private AccessToken accessToken;
	private Configuration jconf;
	
	private ViewPager viewPager;
	
	private CustomAdapter HomeAdapter, MentionAdapter, ListAdapter;
	private ResponseList<twitter4j.Status> home, mention;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().hide();
		HomeAdapter = new CustomAdapter(this);
		MentionAdapter = new CustomAdapter(this);
		ListAdapter = new CustomAdapter(this);
		
		setContentView(R.layout.activity_main);
		
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(new MyFragmentStatePagerAdapter(getSupportFragmentManager(), this));
		viewPager.setCurrentItem(1);
		viewPager.setOffscreenPageLimit(2);
		
		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.mainPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		LogIn(false, this);
	}
	
	public void LogIn(final boolean onlyLogin, final Context context){
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
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					appClass.setMyScreenName(MyScreenName);
					appClass.setTwitter(twitter);
					mentionPattern = Pattern.compile(".*@" + MyScreenName + ".*", Pattern.DOTALL);
					appClass.setMentionPattern(mentionPattern);
					if(!onlyLogin){
						getTimeLine();
						connectStreaming();
					}
				}else
					new ShowToast("スクリーンネームの取得に失敗しました", context, 0);
			}
		};
		
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		appClass = (ApplicationClass)context.getApplicationContext();
		appClass.setHomeAdapter(HomeAdapter);
		appClass.setMentionAdapter(MentionAdapter);
		appClass.setListAdapter(ListAdapter);
		appClass.setList_AlreadyLoad(false);
		
		appClass.setOption_regex(pref.getBoolean("menu_regex", false));
		appClass.setOption_openBrowser(pref.getBoolean("menu_openBrowser", false));
		appClass.setGetBigIcon(pref.getBoolean("getBigIcon", false));
		
		if(pref.getString("AccessToken", "").equals("")){
			startActivity(new Intent(context, startOAuth.class));
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
			
			View customToast = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
			appClass.setToastView(customToast);
			appClass.setToast_Main_Message((TextView)customToast.findViewById(R.id.toast_main_message));
			appClass.setToast_Tweet((TextView)customToast.findViewById(R.id.toast_tweet));
			appClass.setToast_Icon((SmartImageView)customToast.findViewById(R.id.toast_icon));
			
			task.execute();
		}
	}
	
	public void getTimeLine(){
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
			@Override
			protected void onPostExecute(Boolean result){
				if(result){
					for(twitter4j.Status status : home)
						HomeAdapter.add(status);
					for(twitter4j.Status status : mention)
						MentionAdapter.add(status);
				}else
					new ShowToast("タイムライン取得エラー", MainActivity.this, 0);
			}
		};
		task.execute();
		if(pref.getBoolean("startApp_showList", false) && pref.getLong("SelectListId", -1) != -1L)
			getList();
	}
	public void getList(){
		AsyncTask<Void, Void, ResponseList<Status>> task = new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
				try {
					return twitter.getUserListStatuses(pref.getLong("SelectListId", -1), new Paging(1, 50));
				} catch (TwitterException e) {
					return null;
				}
			}
			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result != null){
					for(twitter4j.Status status : result)
						ListAdapter.add(status);
					appClass.setList_AlreadyLoad(true);
				}
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
				@Override
				public void onStatus(final Status status){
					AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
						@Override
						protected Boolean doInBackground(Void... params){
							return true;
						}
						@Override
						protected void onPostExecute(Boolean result){
							android.widget.ListView l = appClass.getHomeList();
							if(l.getChildCount() != 0){
								int pos = l.getFirstVisiblePosition();
								int top = l.getChildAt(0).getTop();
								HomeAdapter.insert(status, 0);
								if(pos == 0 && top == 0)
									l.setSelectionFromTop(pos, 0);
								else
									l.setSelectionFromTop(pos + 1, top);
							}
							
							if(mentionPattern.matcher(status.getText()).find() && !status.isRetweet())
								MentionAdapter.insert(status, 0);
						}
					};
					task.execute();
				}
			};
			//ConnectionLifeCycleListener
			ConnectionLifeCycleListener clcl = new ConnectionLifeCycleListener() {
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
					MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(new String[]{"ユーザー検索", "アカウント", "設定"}, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0){
					AlertDialog.Builder userSearch = new AlertDialog.Builder(MainActivity.this);
					final EditText userEdit = new EditText(MainActivity.this);
					userSearch.setMessage("ユーザーのスクリーンネームを入力してください")
					.setView(userEdit)
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent userPage = new Intent(MainActivity.this, UserPage.class);
							String user_screen = userEdit.getText().toString();
							if(user_screen.isEmpty())
								new ShowToast("なにも入力されていません", MainActivity.this, 0);
							else{
								userPage.putExtra("userScreenName", user_screen.replace("@", ""));
								startActivity(userPage);
							}
						}
					});
					userSearch.create().show();
				}
				if(which == 1){
					SQLiteDatabase db = new SQLHelper(MainActivity.this).getWritableDatabase();
					String[] columns = new String[]{"screen_name", "CK", "CS", "AT", "ATS", "showList", "SelectListId", "SelectListName", "startApp_showList"};
					Cursor result = db.query("accounts", columns, null, null, null, null, null);
					boolean mov = result.moveToFirst();
					List<String> selectAccount_screenName = new ArrayList<String>();
					final List<String> selectAccount_CK = new ArrayList<String>();
					final List<String> selectAccount_CS = new ArrayList<String>();
					final List<String> selectAccount_AT = new ArrayList<String>();
					final List<String> selectAccount_ATS = new ArrayList<String>();
					final List<Boolean> selectAccount_showList = new ArrayList<Boolean>();
					final List<Long> selectAccount_SelectListId = new ArrayList<Long>();
					final List<String> selectAccount_SelectListName = new ArrayList<String>();
					final List<Boolean> selectAccount_startApp_showList = new ArrayList<Boolean>();
					while(mov){
						String screen = "@" + result.getString(0);
						if(screen.equals("@" + MyScreenName))
							screen = screen + " (now)";
						selectAccount_screenName.add(screen);
						selectAccount_CK.add(result.getString(1));
						selectAccount_CS.add(result.getString(2));
						selectAccount_AT.add(result.getString(3));
						selectAccount_ATS.add(result.getString(4));
						selectAccount_showList.add(Boolean.parseBoolean(result.getString(5)));
						selectAccount_SelectListId.add(Long.parseLong(result.getString(6)));
						selectAccount_SelectListName.add(result.getString(7));
						selectAccount_startApp_showList.add(Boolean.parseBoolean(result.getString(8)));
						
						mov = result.moveToNext();
					}
					selectAccount_screenName.add("アカウントを追加");
					AlertDialog.Builder screennameDialog = new AlertDialog.Builder(MainActivity.this);
					final String[] nameDialog = (String[])selectAccount_screenName.toArray(new String[0]);
					screennameDialog.setItems(nameDialog, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(nameDialog[which].equals("アカウントを追加")){
								startActivity(new Intent(MainActivity.this, startOAuth.class));
							}else if(!nameDialog[which].equals("@" + MyScreenName + " (now)")){
								pref.edit()
								.putString("CustomCK", selectAccount_CK.get(which))
								.putString("CustomCS", selectAccount_CS.get(which))
								.putString("AccessToken", selectAccount_AT.get(which))
								.putString("AccessTokenSecret", selectAccount_ATS.get(which))
								.putBoolean("showList", selectAccount_showList.get(which))
								.putLong("SelectListId", selectAccount_SelectListId.get(which))
								.putString("SelectListName", selectAccount_SelectListName.get(which))
								.putBoolean("startApp_showList", selectAccount_startApp_showList.get(which)).commit();
								restart();
							}
						}
					});
					screennameDialog.create().show();
				}
				if(which == 2){
					startActivity(new Intent(MainActivity.this, Preference.class));
				}
			}
		});
		builder.create().show();
	}
	public void restart(){
		resetFlag = true;
		finish();
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
		if(resetFlag){
			resetFlag = false;
			startActivity(new Intent(this, MainActivity.class));
		}
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