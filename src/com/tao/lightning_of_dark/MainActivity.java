package com.tao.lightning_of_dark;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	static ListView HomeList;
	static SharedPreferences pref;
	static String CK, CS, MyScreenName; //MyScreenNameには「＠」は含まれない
	
	static Twitter twitter;
	static TwitterFactory twitterFactory;
	static TwitterStream twitterStream;
	
	static AccessToken accessToken;
	static Configuration jconf;
	
	static CustomAdapter HomeAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
		
		HomeList = (ListView)findViewById(R.id.listView1);
		HomeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Status item = (Status)HomeList.getItemAtPosition(position);
				
				List<String> list = new ArrayList<String>();
				list.add("返信"); list.add("リツイート"); list.add("@" + item.getUser().getScreenName());
				if(item.isRetweet())
					list.add("@" + item.getRetweetedStatus().getUser().getScreenName());
				
				URLEntity[] uentitys = item.getURLEntities();
	            if(uentitys != null && uentitys.length > 0){
	                for(int i = 0; i < uentitys.length; i++){
	                    URLEntity uentity = uentitys[i];
	                    list.add(uentity.getExpandedURL());
	                }
	            }
	            MediaEntity[] mentitys = item.getMediaEntities();
	            if(mentitys != null && mentitys.length > 0){
	                for(int i = 0; i < mentitys.length; i++){
	                    MediaEntity mentity = mentitys[i];
	                    list.add(mentity.getMediaURL());
	                }
	            }
	            final String[] items = (String[])list.toArray(new String[0]);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(item.getUser().getScreenName() + " : " + item.getText().substring(0, 10))
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:
							reply(item);
							break;
						case 1: //ReTweet
							AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
								@Override
								protected Void doInBackground(Void... params) {
									try {
										twitter.retweetStatus(item.getId());
									} catch (TwitterException e) {
										showToast(e.toString());
									}
									return null;
								}
							};
							if(item.getUser().getScreenName().equals(MyScreenName))
								showToast("リツイートできないゾ");
							else
								task.execute();
							
							break;
							
						case 2: //UserPage
//							Intent intent = new Intent(MainActivity.this, UserPage.class);
//							startActivity(intent);
							break;
							
						default:
							if(items[which].startsWith("http")){
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(items[which]));
								startActivity(intent);
							}
						}
					}
				});
				builder.create().show();
			}
		});
		HomeList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Status item = (Status)HomeList.getItemAtPosition(position);
				TweetDialog(item.getText());
				return true;
			}
		});
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
					showToast("スクリーンネームの取得に失敗しました");
					return false;
				}
				return true;
			}
			protected void onPostExecute(Boolean result) {
				if(result){
					getTimeLine();
					connectStreaming();
				}
			}
		};
		task.execute();
	}
	
	public void getTimeLine(){
		HomeAdapter = new CustomAdapter(this);
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
					ResponseList<twitter4j.Status> home = twitter.getHomeTimeline(new Paging(1, 50));
					for(twitter4j.Status status : home)
						HomeAdapter.add(status);
					return true;
				}catch(Exception e){
					showToast(e.toString());
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result)
					HomeList.setAdapter(HomeAdapter);
			}
		};
		task.execute();
	}
	
	public void connectStreaming(){
		try{
			
			TwitterStreamFactory streamFactory = new TwitterStreamFactory(jconf);
			twitterStream = streamFactory.getInstance(accessToken);
			final Handler handler = new Handler();
			//UserStreamAdapter
			UserStreamAdapter streamAdapter = new UserStreamAdapter(){
				public void onStatus(final Status status){
					handler.post(new Runnable(){
						public void run(){
							HomeAdapter.insert(status, 0);
						}
					});
				}
			};
			//ここまで
			twitterStream.addListener(streamAdapter);
			twitterStream.user();
			
		}catch(Exception e){
			showToast("ストリーミング系のエラー\n" + e.toString());
		}
	}
	
	public void tweet(String text, final long id) {
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                	if(id == -1)
                		twitter.updateStatus(params[0]);
                	else
                		twitter.updateStatus(new StatusUpdate(params[0]).inReplyToStatusId(id));
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result)
                	showToast("つぶやきました");
                else
                	showToast("なにかがおかしいよ");
            }
        };
        task.execute(text);
    }
	
	public void TweetDialog(String tweet){
		final EditText tweetText = new EditText(this);
		tweetText.setText(tweet);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("New Tweet")
		.setView(tweetText)
		.setPositiveButton("Tweet!", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				tweet(tweetText.getText().toString(), -1);
			}
		});
		builder.setNegativeButton("キャンセル", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.create().show();
	}
	public void reply(final Status item){
		final EditText tweetText = new EditText(this);
		tweetText.setText("@" + item.getUser().getScreenName() + " ");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String txt = item.getText();
		if(txt.length() > 15)
			txt = txt.substring(0, 15);
		builder.setTitle(item.getUser().getScreenName() + " : " + txt)
		.setView(tweetText)
		.setPositiveButton("Tweet!", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				tweet(tweetText.getText().toString(), item.getId());
			}
		});
		builder.setNegativeButton("キャンセル", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.create().show();
	}
	
	public void new_tweet(View v){
		TweetDialog(null);
	}
	
	public void showToast(String toast){
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
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
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
		case R.id.action_settings:
			return true;
		case R.id.Tweet:
			TweetDialog(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
