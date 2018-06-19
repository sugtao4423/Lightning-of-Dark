package sugtao4423.lod;

import java.util.Arrays;
import java.util.regex.Pattern;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;
import sugtao4423.lod.dataclass.Account;
import sugtao4423.lod.dataclass.Music;
import sugtao4423.lod.dataclass.TwitterList;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.usetime.UseTime;
import sugtao4423.lod.utils.DBUtil;

public class App extends Application{

	private Account account;
	private Typeface fontAwesomeTypeface;
	// MainActivity
	private Twitter twitter;
	private TwitterStream twitterStream;
	private Pattern mentionPattern;
	private UserStreamAdapter userStreamAdapter;
	private ConnectionLifeCycleListener clcl;
	private TwitterList[] lists;
	private Options options;
	private Level level;
	private UseTime useTime;
	private Music music;

	public void resetAccount(){
		this.account = null;
		this.twitter = null;
		this.twitterStream = null;
		this.mentionPattern = null;
		this.lists = null;
	}

	public Account getCurrentAccount(){
		if(account == null){
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			account = new DBUtil(getApplicationContext()).getAccount(pref.getString(Keys.SCREEN_NAME, ""));
			if(account == null){
				account = new Account("", "", "", "", "", false, 0, "", "", "");
			}
		}
		return account;
	}

	public boolean haveAccount(){
		return !(getCurrentAccount().getScreenName().equals("") || getCurrentAccount().getAccessToken().equals("") ||
				getCurrentAccount().getAccessTokenSecret().equals(""));
	}

	private void twitterLogin(){
		String ck, cs;
		if(getCurrentAccount().getConsumerKey().equals("")){
			ck = getString(R.string.CK);
			cs = getString(R.string.CS);
		}else{
			ck = getCurrentAccount().getConsumerKey();
			cs = getCurrentAccount().getConsumerSecret();
		}
		AccessToken accessToken = new AccessToken(getCurrentAccount().getAccessToken(), getCurrentAccount().getAccessTokenSecret());

		Configuration conf = new ConfigurationBuilder().setOAuthConsumerKey(ck).setOAuthConsumerSecret(cs).setTweetModeExtended(true).build();
		Twitter twitter = new TwitterFactory(conf).getInstance(accessToken);
		this.twitter = twitter;
		this.twitterStream = new TwitterStreamFactory(conf).getInstance(accessToken);
		this.mentionPattern = Pattern.compile(".*@" + getCurrentAccount().getScreenName() + ".*", Pattern.DOTALL);
	}

	public void updateStatus(final StatusUpdate status){
		new AsyncTask<Void, Void, Status>(){

			@Override
			protected twitter4j.Status doInBackground(Void... params){
				try{
					return getTwitter().updateStatus(status);
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(twitter4j.Status result){
				if(result != null){
					int exp = getLevel().getRandomExp();
					boolean isLvUp = getLevel().addExp(exp);
					new ShowToast(getApplicationContext(), getString(R.string.success_tweet, exp));
					if(isLvUp)
						new ShowToast(getApplicationContext(), getString(R.string.level_up, getLevel().getLevel()), Toast.LENGTH_LONG);
				}else{
					new ShowToast(getApplicationContext(), R.string.error_tweet);
				}
			}
		}.execute();
	}

	public Typeface getFontAwesomeTypeface(){
		if(fontAwesomeTypeface == null){
			fontAwesomeTypeface = Typeface.createFromAsset(getAssets(), "fontawesome.ttf");
		}
		return fontAwesomeTypeface;
	}

	/*
	 * +-+-+-+-+-+-+-+-+-+-+-+-+
	 * |M|a|i|n|A|c|t|i|v|i|t|y|
	 * +-+-+-+-+-+-+-+-+-+-+-+-+
	 */

	// Twitter
	public Twitter getTwitter(){
		if(twitter == null)
			twitterLogin();
		return twitter;
	}

	// TwitterStream
	public TwitterStream getTwitterStream(){
		if(twitterStream == null)
			twitterLogin();
		return twitterStream;
	}

	// mentionPattern
	public Pattern getMentionPattern(){
		if(mentionPattern == null)
			twitterLogin();
		return mentionPattern;
	}

	// UserStreamAdapter
	public void setUserStreamAdapter(UserStreamAdapter userStreamAdapter){
		this.userStreamAdapter = userStreamAdapter;
	}

	public UserStreamAdapter getUserStreamAdapter(){
		return userStreamAdapter;
	}

	// ConnectionLifeCycleListener
	public void setConnectionLifeCycleListener(ConnectionLifeCycleListener clcl){
		this.clcl = clcl;
	}

	public ConnectionLifeCycleListener getConnectionLifeCycleListener(){
		return clcl;
	}

	// lists
	public TwitterList[] getLists(Context context){
		if(lists == null){
			if(getCurrentAccount().getShowList() &&
					!getCurrentAccount().getStartAppLoadLists().equals("") && !getCurrentAccount().getSelectListIds().equals("")){
				String[] listNames = getCurrentAccount().getSelectListNames().split(",", 0);
				String[] listIds = getCurrentAccount().getSelectListIds().split(",", 0);
				String[] appStartLoadListNames = getCurrentAccount().getStartAppLoadLists().split(",", 0);
				lists = new TwitterList[listNames.length];
				for(int i = 0; i < lists.length; i++){
					TweetListAdapter adapter = new TweetListAdapter(context);
					boolean isAlreadyLoad = false;
					String listName = listNames[i];
					long listId = Long.parseLong(listIds[i]);
					boolean isAppStartLoad = Arrays.asList(appStartLoadListNames).contains(listName);
					lists[i] = new TwitterList(adapter, isAlreadyLoad, listName, listId, isAppStartLoad);
				}
			}else{
				lists = new TwitterList[0];
			}
		}
		return lists;
	}

	// options
	public void loadOption(){
		options = new Options(getApplicationContext());
	}

	public Options getOptions(){
		if(options == null)
			loadOption();
		return options;
	}

	// Level system
	public Level getLevel(){
		if(level == null)
			level = new Level(getApplicationContext());
		return level;
	}

	// UseTime
	public UseTime getUseTime(){
		if(useTime == null)
			useTime = new UseTime(getApplicationContext());
		return useTime;
	}

	// Music
	public void setMusic(Music music){
		this.music = music;
	}

	public Music getMusic(){
		return music;
	}

}