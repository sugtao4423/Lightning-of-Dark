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
import twitter4j.User;
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
	private boolean option_regex, option_openBrowser, isWebm;
	private boolean[] list_AlreadyLoad;
	// MainActivity - CustomToast
	private View customToast;
	private TextView toast_main_message, toast_tweet;
	private SmartImageView toast_icon;

	// UserPage
	private User target;
	private String targetScreenName;

	public void twitterLogin(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String ck, cs;
		if(pref.getString("CustomCK", "").equals("")){
			ck = getString(R.string.CK);
			cs = getString(R.string.CS);
		}else{
			ck = pref.getString("CustomCK", null);
			cs = pref.getString("CustomCS", null);
		}
		AccessToken accessToken = new AccessToken(pref.getString("AccessToken", ""), pref.getString("AccessTokenSecret", ""));

		Configuration conf = new ConfigurationBuilder().setOAuthConsumerKey(ck).setOAuthConsumerSecret(cs).setTweetModeExtended(true).build();
		Twitter twitter = new TwitterFactory(conf).getInstance(accessToken);
		this.myScreenName = pref.getString("ScreenName", "");
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
					new ShowToast("ツイートしました", context, 0);
				else
					new ShowToast("ツイートできませんでした", context, 0);
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

	// isWebm
	public void setIsWebm(boolean isWebm){
		this.isWebm = isWebm;
	}

	public boolean getIsWebm(){
		return isWebm;
	}

	public void loadOption(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		setOption_regex(pref.getBoolean("menu_regex", false));
		setOption_openBrowser(pref.getBoolean("menu_openBrowser", false));
		setIsWebm(pref.getBoolean("isWebm", false));
	}

	// list_AlreadyLoad
	public void setList_AlreadyLoad(boolean[] AlreadyLoad){
		this.list_AlreadyLoad = AlreadyLoad;
	}

	public boolean[] getList_AlreadyLoad(){
		return list_AlreadyLoad;
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

	/*
	 * +-+-+-+-+-+-+-+-+
	 * |U|s|e|r|P|a|g|e|
	 * +-+-+-+-+-+-+-+-+
	 */
	public void setTarget(User target){
		this.target = target;
	}

	public User getTarget(){
		return target;
	}

	public void setTargetScreenName(String targetScreenName){
		this.targetScreenName = targetScreenName;
	}

	public String getTargetScreenName(){
		return targetScreenName;
	}
}