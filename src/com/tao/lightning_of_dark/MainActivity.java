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
	private TwitterStream twitterStream;
	private Pattern mentionPattern;
	
	private ApplicationClass appClass;
	
	private SharedPreferences pref;
	private boolean resetFlag;
	
	
	private AccessToken accessToken;
	private Configuration conf;
	
	private ViewPager viewPager;
	
	private CustomAdapter HomeAdapter, MentionAdapter;
	private CustomAdapter[] ListAdapters;
	private ResponseList<twitter4j.Status> home, mention;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().hide();
		HomeAdapter = new CustomAdapter(this);
		MentionAdapter = new CustomAdapter(this);
		
		setContentView(R.layout.activity_main);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(new MyFragmentStatePagerAdapter(getSupportFragmentManager(), this));
		viewPager.setCurrentItem(1);
		viewPager.setOffscreenPageLimit(pref.getInt("SelectListCount", 0) + 1);
		ListAdapters = new CustomAdapter[pref.getInt("SelectListCount", 0)];
		for(int i = 0; i < pref.getInt("SelectListCount", 0); i++)
			ListAdapters[i] = new CustomAdapter(this);
		
		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.mainPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		LogIn();
	}
	
	public void LogIn(){
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				conf = new ConfigurationBuilder()
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
					getTimeLine();
					connectStreaming();
				}else
					new ShowToast("スクリーンネームの取得に失敗しました", MainActivity.this, 0);
			}
		};
		appClass = (ApplicationClass)getApplicationContext();
		appClass.setHomeAdapter(HomeAdapter);
		appClass.setMentionAdapter(MentionAdapter);
		appClass.setListAdapters(ListAdapters);
		appClass.setList_AlreadyLoad(false);
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
		if(pref.getBoolean("startApp_showList", false) && pref.getString("SelectListIds", null) != null){
			String[] listIds_str = pref.getString("SelectListIds", "").split(",", 0);
			long[] listIds = new long[listIds_str.length];
			for(int i = 0; i < listIds_str.length; i++)
				listIds[i] = Long.parseLong(listIds_str[i]);
			
			for(int i = 0; i < listIds.length; i++)
				getList(listIds[i], i);
		}
	}
	public void getList(final long listId, final int index){
		AsyncTask<Void, Void, ResponseList<Status>> task = new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
				try {
					return twitter.getUserListStatuses(listId, new Paging(1, 50));
				} catch (TwitterException e) {
					return null;
				}
			}
			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result != null){
					for(twitter4j.Status status : result)
						ListAdapters[index].add(status);
					appClass.setList_AlreadyLoad(true);
				}
			}
		};
		task.execute();
	}
	
	public void connectStreaming(){
		try{
			twitterStream = new TwitterStreamFactory(conf).getInstance(accessToken);
			
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
		final String[] items = new String[]{"ツイート爆撃", "ユーザー検索", "アカウント", "設定"};
		builder.setItems(items, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(items[which].equals("ユーザー検索")){
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
				if(items[which].equals("アカウント")){
					SQLiteDatabase db = new SQLHelper(MainActivity.this).getWritableDatabase();
					String[] columns = new String[]{"screen_name", "CK", "CS", "AT", "ATS", "showList", "SelectListCount", "SelectListIds", "SelectListNames", "startApp_showList"};
					Cursor result = db.query("accounts", columns, null, null, null, null, null);
					boolean mov = result.moveToFirst();
					List<String> selectAccount_screenName = new ArrayList<String>();
					final List<String> selectAccount_CK = new ArrayList<String>();
					final List<String> selectAccount_CS = new ArrayList<String>();
					final List<String> selectAccount_AT = new ArrayList<String>();
					final List<String> selectAccount_ATS = new ArrayList<String>();
					final List<Boolean> selectAccount_showList = new ArrayList<Boolean>();
					final List<Integer> selectAccount_SelectListCount = new ArrayList<Integer>();
					final List<String> selectAccount_SelectListIds = new ArrayList<String>();
					final List<String> selectAccount_SelectListNames = new ArrayList<String>();
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
						selectAccount_SelectListCount.add(Integer.parseInt(result.getString(6)));
						selectAccount_SelectListIds.add(result.getString(7));
						selectAccount_SelectListNames.add(result.getString(8));
						selectAccount_startApp_showList.add(Boolean.parseBoolean(result.getString(9)));
						
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
								.putInt("SelectListCount", selectAccount_SelectListCount.get(which))
								.putString("SelectListIds", selectAccount_SelectListIds.get(which))
								.putString("SelectListNames", selectAccount_SelectListNames.get(which))
								.putBoolean("startApp_showList", selectAccount_startApp_showList.get(which)).commit();
								restart();
							}
						}
					});
					screennameDialog.create().show();
				}
				if(items[which].equals("設定")){
					startActivity(new Intent(MainActivity.this, Preference.class));
				}
				if(items[which].equals("ツイート爆撃")){
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					final View bombView = getLayoutInflater().inflate(R.layout.tweet_bomb, null);
					builder.setView(bombView);
					builder.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EditText _staticText = (EditText)bombView.findViewById(R.id.bomb_staticText);
							EditText _loopText = (EditText)bombView.findViewById(R.id.bomb_loopText);
							EditText _loopCount = (EditText)bombView.findViewById(R.id.bomb_loopCount);
							
							final String staticText = _staticText.getText().toString();
							final String loopText = _loopText.getText().toString();
							int loopCount = Integer.parseInt(_loopCount.getText().toString());
							
							String loop = "";
							for(int i = 0; i < loopCount; i++){
								loop += loopText;
								AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>(){
									@Override
									protected Void doInBackground(String... params) {
										try {
											twitter.updateStatus(staticText + params[0]);
										} catch (TwitterException e) {}
										return null;
									}
								};
								task.execute(loop);
							}
							new ShowToast("ツイート完了", MainActivity.this, 0);
						}
					});
					builder.setNegativeButton("キャンセル", null);
					builder.create().show();
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
		if(resetFlag){
			resetFlag = false;
			startActivity(new Intent(this, MainActivity.class));
		}else{
			task.execute();
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