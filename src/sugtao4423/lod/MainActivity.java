package sugtao4423.lod;

import java.io.File;
import java.util.regex.Pattern;

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
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;
import sugtao4423.icondialog.IconDialog;
import sugtao4423.icondialog.IconItem;
import sugtao4423.lod.dataclass.TwitterList;
import sugtao4423.lod.main_fragment.Fragment_home;
import sugtao4423.lod.main_fragment.Fragment_mention;
import sugtao4423.lod.main_fragment.MainFragmentPagerAdapter;

public class MainActivity extends FragmentActivity{

	private Twitter twitter;
	private TwitterStream twitterStream;
	private Pattern mentionPattern;

	private App app;
	private boolean resetFlag;

	private Fragment_mention fragmentMention;
	private Fragment_home fragmentHome;

	private Builder iconDialog;

	private MusicReceiver musicReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_main);
		app = (App)getApplicationContext();

		MainFragmentPagerAdapter pagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), this);

		ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(1);
		viewPager.setOffscreenPageLimit(app.getCurrentAccount().getSelectListCount() + 1);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.mainPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor(getString(R.color.pagerTabText)));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		fragmentHome = pagerAdapter.getFragmentHome();
		fragmentMention = pagerAdapter.getFragmentMention();
		logIn();
		setMusicReceiver();
	}

	public void logIn(){
		if(!app.haveAccount()){
			startActivity(new Intent(this, StartOAuth.class));
			finish();
		}else{
			twitter = app.getTwitter();
			twitterStream = app.getTwitterStream();
			mentionPattern = app.getMentionPattern();
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
					new ShowToast(getApplicationContext(), R.string.error_getTimeLine);
				}else{
					fragmentHome.addAll(result);
				}
			}
		}.execute();
		getList();
	}

	public void getList(){
		TwitterList[] lists = app.getLists(this);
		for(int i = 0; i < lists.length; i++){
			final TwitterList list = lists[i];
			if(!list.getIsAppStartLoad()){
				return;
			}
			new AsyncTask<Void, Void, ResponseList<Status>>(){
				@Override
				protected ResponseList<twitter4j.Status> doInBackground(Void... params){
					try{
						return twitter.getUserListStatuses(list.getListId(), new Paging(1, 50));
					}catch(TwitterException e){
						return null;
					}
				}

				@Override
				protected void onPostExecute(ResponseList<twitter4j.Status> result){
					if(result != null){
						list.getTweetListAdapter().addAll(result);
						list.setIsAlreadyLoad(true);
					}
				}
			}.execute();
		}
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
							new ShowToast(getApplicationContext(), text);
						}
					});
				}
			};
			twitterStream.addListener(streamAdapter);
			twitterStream.addConnectionLifeCycleListener(clcl);
			twitterStream.user();
		}catch(Exception e){
			new ShowToast(getApplicationContext(), getString(R.string.error_streaming) + "\n" + e.toString(), Toast.LENGTH_LONG);
		}
	}

	public void setMusicReceiver(){
		musicReceiver = new MusicReceiver();
		IntentFilter filter = new IntentFilter();
		for(String s : MusicReceiver.ACTIONS_GOOGLEPLAY){
			filter.addAction(s);
		}
		for(String s : MusicReceiver.ACTIONS_SONYMUSIC){
			filter.addAction(s);
		}
		registerReceiver(musicReceiver, filter);
	}

	public void new_tweet(View v){
		Intent intent = new Intent(MainActivity.this, TweetActivity.class);
		startActivity(intent);
	}

	public void option(View v){
		if(iconDialog == null){
			int black = Color.parseColor(getString(R.color.icon));
			IconItem[] items = new IconItem[7];
			items[0] = new IconItem(getString(R.string.icon_bomb).charAt(0), black, "ツイート爆撃");
			items[1] = new IconItem(getString(R.string.icon_search).charAt(0), black, "ユーザー検索");
			items[2] = new IconItem(getString(R.string.icon_refresh).charAt(0), black, "Homeを更新");
			items[3] = new IconItem(getString(R.string.icon_user).charAt(0), black, "アカウント");
			items[4] = new IconItem(getString(R.string.icon_experience).charAt(0), black, "レベル情報");
			items[5] = new IconItem(getString(R.string.icon_clock).charAt(0), black, "使用情報");
			items[6] = new IconItem(getString(R.string.icon_cog).charAt(0), black, "設定");
			iconDialog = new IconDialog(this).setItems(items, new OptionClickListener(this));
		}
		iconDialog.show();
	}

	public void restart(){
		resetFlag = true;
		finish();
	}

	@Override
	public void onResume(){
		super.onResume();
		app.getUseTime().start();
	}

	@Override
	public void onPause(){
		super.onPause();
		app.getUseTime().stop();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(musicReceiver);
		app.resetCurrentAccount();
		app.resetTwitter();
		app.resetLists();
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