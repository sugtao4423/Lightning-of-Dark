package sugtao4423.lod;

import java.util.HashMap;
import java.util.regex.Matcher;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;
import sugtao4423.lod.userpage_fragment.UserPage;
import sugtao4423.lod.utils.Regex;

public class IntentActivity extends Activity{

	public static final String TWEET_ID = "tweetId";

	private App app;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		app = (App)getApplicationContext();

		if(!app.haveAccount()){
			startActivity(new Intent(this, StartOAuth.class));
			finish();
			return;
		}

		long tweetId = getIntent().getLongExtra(TWEET_ID, -1);
		if(tweetId == -1){
			jump();
		}else{
			showStatus(tweetId);
		}
	}

	public void jump(){
		if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
			String uri = getIntent().getData().toString();
			Matcher status = Regex.statusUrl.matcher(uri);
			Matcher share = Regex.shareUrl.matcher(uri);
			Matcher user = Regex.userUrl.matcher(uri);
			if(status.find()){
				showStatus(Long.parseLong(status.group(2)));
			}else if(share.find()){
				Uri shareUri = Uri.parse(uri);
				HashMap<String, String> map = new HashMap<String, String>();
				for(String name : shareUri.getQueryParameterNames()){
					map.put(name, shareUri.getQueryParameter(name));
				}
				String text = (map.get("text") == null ? "" : (map.get("text") + " ")) +
						(map.get("url") == null ? "" : (map.get("url") + " ")) +
						(map.get("hashtags") == null ? "" : ("#" + map.get("hashtags").replace(",", " #") + " ")) +
						(map.get("via") == null ? "" : ("@" + map.get("via") + "さんから "));
				text = text.substring(0, text.length() - 1);
				Intent i = new Intent(IntentActivity.this, TweetActivity.class);
				i.putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_EXTERNALTEXT);
				i.putExtra(TweetActivity.INTENT_EXTRA_KEY_TEXT, text);
				startActivity(i);
				finish();
			}else if(user.find()){
				Intent i = new Intent(IntentActivity.this, UserPage.class);
				i.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, user.group(2));
				startActivity(i);
				finish();
			}
		}else if(Intent.ACTION_SEND.equals(getIntent().getAction())){
			String subject = getIntent().getExtras().getString(Intent.EXTRA_SUBJECT);
			String text = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
			text = (subject == null) ? text : (subject.isEmpty() ? text : (subject + " " + text));
			Intent i = new Intent(IntentActivity.this, TweetActivity.class);
			i.putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_EXTERNALTEXT);
			i.putExtra(TweetActivity.INTENT_EXTRA_KEY_TEXT, text);
			startActivity(i);
			finish();
		}
	}

	public void showStatus(long tweetId){
		new AsyncTask<Long, Void, Status>(){
			@Override
			protected twitter4j.Status doInBackground(Long... params){
				try{
					return app.getTwitter().showStatus(params[0]);
				}catch(TwitterException e){
					return null;
				}
			}

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
			@Override
			protected void onPostExecute(twitter4j.Status status){
				if(status != null){
					TweetListView l = new TweetListView(IntentActivity.this);
					TweetListAdapter adapter = new TweetListAdapter(IntentActivity.this);
					adapter.add(status);
					l.setAdapter(adapter);
					adapter.setOnItemClickListener(new ListViewListener());
					adapter.setOnItemLongClickListener(new ListViewListener());
					AlertDialog.Builder builder = new AlertDialog.Builder(IntentActivity.this);
					builder.setView(l);
					builder.setOnDismissListener(new OnDismissListener(){
						@Override
						public void onDismiss(DialogInterface dialog){
							finish();
						}
					});
					AlertDialog dialog = builder.create();
					dialog.getWindow().setDimAmount(0f);
					dialog.show();
				}else{
					new ShowToast(getApplicationContext(), R.string.error_getStatus);
				}
			}
		}.execute(tweetId);
	}
}