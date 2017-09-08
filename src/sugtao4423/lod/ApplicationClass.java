package sugtao4423.lod;

import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import sugtao4423.lod.tweetlistview.TweetListAdapter;

public class ApplicationClass extends Application{

	// MainActivity
	private String myScreenName;
	private Twitter twitter;
	private TwitterStream twitterStream;
	private Pattern mentionPattern;
	private TweetListAdapter[] listAdapters;
	private boolean option_regex, option_openBrowser, option_millisecond, isWebm;
	private boolean[] listAlreadyLoad;
	// MainActivity - CustomToast
	private View customToast;
	private TextView toast_main_message, toast_tweet;
	private SmartImageView toast_icon;

	public void twitterLogin(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String ck, cs;
		if(pref.getString(Keys.CUSTOM_CK, "").equals("")){
			ck = getString(R.string.CK);
			cs = getString(R.string.CS);
		}else{
			ck = pref.getString(Keys.CUSTOM_CK, null);
			cs = pref.getString(Keys.CUSTOM_CS, null);
		}
		AccessToken accessToken = new AccessToken(pref.getString(Keys.ACCESS_TOKEN, ""), pref.getString(Keys.ACCESS_TOKEN_SECRET, ""));

		Configuration conf = new ConfigurationBuilder().setOAuthConsumerKey(ck).setOAuthConsumerSecret(cs).setTweetModeExtended(true).build();
		Twitter twitter = new TwitterFactory(conf).getInstance(accessToken);
		this.myScreenName = pref.getString(Keys.SCREEN_NAME, "");
		this.twitter = twitter;
		this.twitterStream = new TwitterStreamFactory(conf).getInstance(accessToken);
		this.mentionPattern = Pattern.compile(".*@" + myScreenName + ".*", Pattern.DOTALL);
	}

	public void updateStatus(final Context context, final StatusUpdate status){
		new AsyncTask<Void, Void, Status>(){

			@Override
			protected twitter4j.Status doInBackground(Void... params){
				try{
					return twitter.updateStatus(status);
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(twitter4j.Status result){
				if(result != null)
					new ShowToast(R.string.success_tweet, context, 0);
				else
					new ShowToast(R.string.error_tweet, context, 0);
			}
		}.execute();
	}

	/*
	 * +-+-+-+-+-+-+-+-+-+-+-+-+
	 * |M|a|i|n|A|c|t|i|v|i|t|y|
	 * +-+-+-+-+-+-+-+-+-+-+-+-+
	 */
	// MyScreenName
	public String getMyScreenName(){
		return myScreenName;
	}

	// Twitter
	public Twitter getTwitter(){
		return twitter;
	}

	// TwitterStream
	public TwitterStream getTwitterStream(){
		return twitterStream;
	}

	// mentionPattern
	public Pattern getMentionPattern(){
		return mentionPattern;
	}

	// listAdapter
	public void setListAdapters(TweetListAdapter[] listAdapters){
		this.listAdapters = listAdapters;
	}

	public TweetListAdapter[] getListAdapters(){
		return listAdapters;
	}

	// option_regex
	public void setOption_regex(boolean option_regex){
		this.option_regex = option_regex;
	}

	public boolean getOption_regex(){
		return option_regex;
	}

	// option_openBrowser
	public void setOption_openBrowser(boolean option_openBrowser){
		this.option_openBrowser = option_openBrowser;
	}

	public boolean getOption_openBrowser(){
		return option_openBrowser;
	}

	// option_millisecond
	public void setOption_millisecond(boolean option_millisecond){
		this.option_millisecond = option_millisecond;
	}

	public boolean getOption_millisecond(){
		return option_millisecond;
	}

	// isWebm
	public void setIsWebm(boolean isWebm){
		this.isWebm = isWebm;
	}

	public boolean getIsWebm(){
		return isWebm;
	}

	public void loadOption(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		setOption_regex(pref.getBoolean(Keys.MENU_REGEX, false));
		setOption_openBrowser(pref.getBoolean(Keys.MENU_OPEN_BROWSER, false));
		setOption_millisecond(pref.getBoolean(Keys.MENU_MILLISECOND, false));
		setIsWebm(pref.getBoolean(Keys.IS_WEBM, false));
	}

	// list_AlreadyLoad
	public void setListAlreadyLoad(boolean[] AlreadyLoad){
		this.listAlreadyLoad = AlreadyLoad;
	}

	public boolean[] getListAlreadyLoad(){
		return listAlreadyLoad;
	}

	// CustomToast
	public void setToastView(View custom_toast){
		this.customToast = custom_toast;
	}

	public View getToastView(){
		return customToast;
	}

	public void setToast_Main_Message(TextView toast_main_message){
		this.toast_main_message = toast_main_message;
	}

	public TextView getToast_Main_Message(){
		return toast_main_message;
	}

	public void setToast_Tweet(TextView toast_tweet){
		this.toast_tweet = toast_tweet;
	}

	public TextView getToast_Tweet(){
		return toast_tweet;
	}

	public void setToast_Icon(SmartImageView toast_icon){
		this.toast_icon = toast_icon;
	}

	public SmartImageView getToast_Icon(){
		return toast_icon;
	}

}