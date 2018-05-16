package sugtao4423.lod;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.UserStreamAdapter;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import sugtao4423.icondialog.IconDialog;
import sugtao4423.icondialog.IconItem;
import sugtao4423.lod.main_fragment.Fragment_home;
import sugtao4423.lod.main_fragment.Fragment_mention;
import sugtao4423.lod.main_fragment.MyFragmentStatePagerAdapter;
import sugtao4423.lod.tweetlistview.TweetListAdapter;

public class MainActivity extends FragmentActivity{

	private Twitter twitter;
	private TwitterStream twitterStream;
	private Pattern mentionPattern;

	private ApplicationClass appClass;

	private SharedPreferences pref;
	private boolean resetFlag;

	private TweetListAdapter[] listAdapters;

	private Fragment_mention fragmentMention;
	private Fragment_home fragmentHome;

	private Builder iconDialog;

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
		viewPager.setOffscreenPageLimit(pref.getInt(Keys.SELECT_LIST_COUNT, 0) + 1);
		listAdapters = new TweetListAdapter[pref.getInt(Keys.SELECT_LIST_COUNT, 0)];
		for(int i = 0; i < pref.getInt(Keys.SELECT_LIST_COUNT, 0); i++)
			listAdapters[i] = new TweetListAdapter(this);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.mainPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor(getString(R.color.pagerTabText)));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		fragmentHome = pagerAdapter.getFragmentHome();
		fragmentMention = pagerAdapter.getFragmentMention();
		logIn();
	}

	public void logIn(){
		appClass = (ApplicationClass)getApplicationContext();
		appClass.setListAdapters(listAdapters);
		boolean[] list_alreadyLoad = new boolean[listAdapters.length];
		for(int i = 0; i < listAdapters.length; i++)
			list_alreadyLoad[i] = false;
		appClass.setListAlreadyLoad(list_alreadyLoad);

		View customToast = View.inflate(this, R.layout.custom_toast, null);
		appClass.setToastView(customToast);
		appClass.setToast_Main_Message((TextView)customToast.findViewById(R.id.toast_main_message));
		appClass.setToast_Tweet((TextView)customToast.findViewById(R.id.toast_tweet));
		appClass.setToast_Icon((SmartImageView)customToast.findViewById(R.id.toast_icon));

		if(pref.getString(Keys.ACCESS_TOKEN, "").equals("")){
			startActivity(new Intent(this, StartOAuth.class));
			finish();
		}else{
			twitter = appClass.getTwitter();
			twitterStream = appClass.getTwitterStream();
			mentionPattern = appClass.getMentionPattern();
			getTimeLine();
			connectStreaming();
		}
	}

	public void getTimeLine(){
		fragmentHome.clear();
		new AsyncTask<Void, Void, ResponseList<Status>>(){

			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params){
				try{
					return twitter.getHomeTimeline(new Paging(1, 50));
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result == null){
					new ShowToast(R.string.error_getTimeLine, MainActivity.this, 0);
				}else{
					fragmentHome.addAll(result);
				}
			}
		}.execute();

		if(!pref.getString(Keys.APP_START_LOAD_LISTS, "").equals("") && !pref.getString(Keys.SELECT_LIST_IDS, "").equals("")){
			String[] listName_str = pref.getString(Keys.SELECT_LIST_NAMES, "").split(",", 0);
			String[] listIds_str = pref.getString(Keys.SELECT_LIST_IDS, "").split(",", 0);
			String[] startApp_loadLists = pref.getString(Keys.APP_START_LOAD_LISTS, "").split(",", 0);
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
				if(result != null){
					listAdapters[index].addAll(result);
					boolean[] tmp = appClass.getListAlreadyLoad();
					tmp[index] = true;
					appClass.setListAlreadyLoad(tmp);
				}
			}
		}.execute();
	}

	public void connectStreaming(){
		try{
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
			new ShowToast(R.string.error_streaming + "\n" + e.toString(), MainActivity.this, 0);
		}
	}

	public void new_tweet(View v){
		Intent intent = new Intent(MainActivity.this, TweetActivity.class);
		startActivity(intent);
	}

	public void option(View v){
		if(iconDialog == null){
			int black = Color.rgb(97, 97, 97);
			IconItem[] items = new IconItem[5];
			items[0] = new IconItem(getString(R.string.icon_bomb).charAt(0), black, "ツイート爆撃");
			items[1] = new IconItem(getString(R.string.icon_search).charAt(0), black, "ユーザー検索");
			items[2] = new IconItem(getString(R.string.icon_refresh).charAt(0), black, "Homeを更新");
			items[3] = new IconItem(getString(R.string.icon_user).charAt(0), black, "アカウント");
			items[4] = new IconItem(getString(R.string.icon_cog).charAt(0), black, "設定");
			iconDialog = new IconDialog(this, "fontawesome.ttf").setItems(items, new OptionClickListener(this));
		}
		iconDialog.show();
	}

	public void restart(){
		resetFlag = true;
		finish();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		appClass.resetTwitter();
		if(resetFlag){
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
		clearThumbnailCache();
	}

	public void clearThumbnailCache(){
		File cache = new File(getCacheDir().getAbsolutePath() + "/web_image_cache/");
		if(!cache.exists())
			return;
		for(File f : cache.listFiles()){
			if(f.getName().startsWith("http+pbs+twimg+com+media+"))
				f.delete();
		}
	}
}