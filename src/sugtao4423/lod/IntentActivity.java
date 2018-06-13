package sugtao4423.lod;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;
import sugtao4423.lod.userpage_fragment.UserPage;
import sugtao4423.lod.utils.Regex;

public class IntentActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		App app = (App)getApplicationContext();

		if(app.getTwitter() == null){
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			app = (App)this.getApplicationContext();

			if(pref.getString(Keys.ACCESS_TOKEN, "").equals("")){
				startActivity(new Intent(this, StartOAuth.class));
				finish();
			}else{
				jump();
			}
		}else{
			jump();
		}
	}

	public void jump(){
		if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
			String uri = getIntent().getData().toString();
			Matcher status = Regex.statusUrl.matcher(uri);
			Matcher share = Regex.shareUrl.matcher(uri);
			Matcher user = Regex.userUrl.matcher(uri);
			if(status.find()){
				showStatus(Long.parseLong(status.group(2)), IntentActivity.this, true);
			}else if(share.find()){
				List<NameValuePair> params;
				try{
					params = URLEncodedUtils.parse(new URI(uri), "UTF-8");
				}catch(URISyntaxException e){
					new ShowToast(getApplicationContext(), R.string.urlNotMatchPattern);
					finish();
					return;
				}
				HashMap<String, String> map = new HashMap<String, String>();
				for(NameValuePair param : params)
					map.put(param.getName(), param.getValue());
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

	public void showStatus(long tweetId, final Context context, final boolean isClose){
		final App app = (App)context.getApplicationContext();
		new AsyncTask<Long, Void, Status>(){
			@Override
			protected twitter4j.Status doInBackground(Long... params){
				try{
					return app.getTwitter().showStatus(params[0]);
				}catch(TwitterException e){
					return null;
				}
			}

			@SuppressLint("NewApi")
			@Override
			protected void onPostExecute(twitter4j.Status status){
				if(status != null){
					TweetListView l = new TweetListView(context);
					TweetListAdapter adapter = new TweetListAdapter(context);
					adapter.add(status);
					l.setAdapter(adapter);
					adapter.setOnItemClickListener(new ListViewListener());
					adapter.setOnItemLongClickListener(new ListViewListener());
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setView(l);
					if(isClose){
						builder.setOnDismissListener(new OnDismissListener(){
							@Override
							public void onDismiss(DialogInterface dialog){
								((Activity)context).finish();
							}
						});
					}
					builder.show();
				}else{
					new ShowToast(getApplicationContext(), R.string.error_getStatus);
				}
			}
		}.execute(tweetId);
	}
}