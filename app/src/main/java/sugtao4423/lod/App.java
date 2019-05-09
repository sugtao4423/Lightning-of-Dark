package sugtao4423.lod;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Arrays;
import java.util.regex.Pattern;

import sugtao4423.lod.AutoLoadTLService.AutoLoadTLListener;
import sugtao4423.lod.dataclass.Account;
import sugtao4423.lod.dataclass.TwitterList;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.usetime.UseTime;
import sugtao4423.lod.utils.DBUtil;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class App extends Application{

    private Account account;
    private Typeface fontAwesomeTypeface;
    // MainActivity
    private Twitter twitter;
    private Pattern mentionPattern;
    private AutoLoadTLListener autoLoadTLListener;
    private long latestTweetId;
    private ConnectionLifeCycleListener clcl;
    private TwitterList[] lists;
    private Options options;
    private Level level;
    // Database
    private DBUtil accountDBUtil;
    private UseTime useTime;

    public void resetAccount(){
        this.account = null;
        this.twitter = null;
        this.mentionPattern = null;
        this.lists = null;
    }

    public void reloadAccountFromDB(){
        this.account = null;
    }

    public Account getCurrentAccount(){
        if(account == null){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            account = getAccountDBUtil().getAccount(pref.getString(Keys.SCREEN_NAME, ""));
            if(account == null){
                account = new Account("", "", "", "", "");
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
                    new ShowToast(getApplicationContext(), getString(R.string.param_success_tweet, exp));
                    if(isLvUp){
                        new ShowToast(getApplicationContext(), getString(R.string.param_level_up, getLevel().getLevel()), Toast.LENGTH_LONG);
                    }
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
        if(twitter == null){
            twitterLogin();
        }
        return twitter;
    }

    // mentionPattern
    public Pattern getMentionPattern(){
        if(mentionPattern == null){
            twitterLogin();
        }
        return mentionPattern;
    }

    // AutoLoadTLListener
    public void setAutoLoadTLListener(AutoLoadTLListener autoLoadTLListener){
        this.autoLoadTLListener = autoLoadTLListener;
    }

    public AutoLoadTLListener getAutoLoadTLListener(){
        return autoLoadTLListener;
    }

    // LatestTweetId
    public void setLatestTweetId(long latestTweetId){
        this.latestTweetId = latestTweetId;
    }

    public long getLatestTweetId(){
        return latestTweetId;
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
            String[] listNames = getCurrentAccount().getSelectListNames();
            long[] listIds = getCurrentAccount().getSelectListIds();
            String[] appStartLoadListNames = getCurrentAccount().getStartAppLoadLists();
            lists = new TwitterList[listNames.length];
            for(int i = 0; i < lists.length; i++){
                TweetListAdapter adapter = new TweetListAdapter(context);
                String listName = listNames[i];
                boolean isAppStartLoad = Arrays.asList(appStartLoadListNames).contains(listName);
                lists[i] = new TwitterList(adapter, false, listName, listIds[i], isAppStartLoad);
            }
        }
        return lists;
    }

    // options
    public void loadOption(){
        options = new Options(getApplicationContext());
    }

    public Options getOptions(){
        if(options == null){
            loadOption();
        }
        return options;
    }

    // Level system
    public Level getLevel(){
        if(level == null){
            level = new Level(getApplicationContext());
        }
        return level;
    }

    // DBUtil
    public DBUtil getAccountDBUtil(){
        if(accountDBUtil == null){
            accountDBUtil = new DBUtil(getApplicationContext());
        }
        return accountDBUtil;
    }

    public void closeAccountDB(){
        if(accountDBUtil != null){
            accountDBUtil.dbClose();
            accountDBUtil = null;
        }
    }

    // UseTime
    public UseTime getUseTime(){
        if(useTime == null){
            useTime = new UseTime(getApplicationContext());
        }
        return useTime;
    }

    public void closeUseTimeDB(){
        if(useTime != null){
            useTime.dbClose();
            useTime = null;
        }
    }

}