package sugtao4423.lod;

import java.io.File;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserStreamAdapter;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import sugtao4423.icondialog.IconDialog;
import sugtao4423.icondialog.IconItem;
import sugtao4423.lod.AutoLoadTLService.AutoLoadTLListener;
import sugtao4423.lod.dataclass.TwitterList;
import sugtao4423.lod.main_fragment.Fragment_home;
import sugtao4423.lod.main_fragment.Fragment_mention;
import sugtao4423.lod.main_fragment.MainFragmentPagerAdapter;

public class MainActivity extends FragmentActivity{

	private App app;
	private Twitter twitter;
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
		viewPager.setOffscreenPageLimit(app.getCurrentAccount().getSelectListIds().length + 1);

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
			getList();
			connectStreaming();
			autoLoadTL();
		}
	}

	public void getList(){
		TwitterList[] lists = app.getLists(this);
		for(int i = 0; i < lists.length; i++){
			final TwitterList list = lists[i];
			if(!list.getIsAppStartLoad()){
				continue;
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
		UserStreamAdapter streamAdapter = new UserStreamAdapter(){
			@Override
			public void onStatus(final Status status){
				fragmentHome.insert(status);
				if(app.getMentionPattern().matcher(status.getText()).find() && !status.isRetweet())
					fragmentMention.insert(status);
			}
		};
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
		Intent intent = new Intent(this, UserStreamService.class);
		app.setUserStreamAdapter(streamAdapter);
		app.setConnectionLifeCycleListener(clcl);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			startForegroundService(intent);
		}else{
			startService(intent);
		}
	}

	public void autoLoadTL(){
		if(app.getCurrentAccount().getAutoLoadTLInterval() == 0){
			return;
		}
		AutoLoadTLListener listener = new AutoLoadTLListener(){

			@Override
			public void onStatus(ResponseList<Status> statuses){
				for(Status s : statuses){
					fragmentHome.insert(s);
					if(app.getMentionPattern().matcher(s.getText()).find() && !s.isRetweet()){
						fragmentMention.insert(s);
					}
				}
			}
		};
		app.setAutoLoadTLListener(listener);
		Intent intent = new Intent(this, AutoLoadTLService.class);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			startForegroundService(intent);
		}else{
			startService(intent);
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
			IconItem[] items = new IconItem[6];
			items[0] = new IconItem(getString(R.string.icon_bomb).charAt(0), black, "ツイート爆撃");
			items[1] = new IconItem(getString(R.string.icon_search).charAt(0), black, "ユーザー検索");
			items[2] = new IconItem(getString(R.string.icon_user).charAt(0), black, "アカウント");
			items[3] = new IconItem(getString(R.string.icon_experience).charAt(0), black, "レベル情報");
			items[4] = new IconItem(getString(R.string.icon_clock).charAt(0), black, "使用情報");
			items[5] = new IconItem(getString(R.string.icon_cog).charAt(0), black, "設定");
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
		stopService(new Intent(this, UserStreamService.class));
		stopService(new Intent(this, AutoLoadTLService.class));
		app.resetAccount();
		if(resetFlag){
			resetFlag = false;
			startActivity(new Intent(this, MainActivity.class));
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