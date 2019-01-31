package sugtao4423.lod;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import sugtao4423.lod.dataclass.Account;
import sugtao4423.lod.utils.DBUtil;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class StartOAuth extends AppCompatActivity{

    public static final String CALLBACK_URL = "https://localhost/sugtao4423.lod/oauth";

    private App app;
    private EditText customCK, customCS;
    private Button ninsyobtn;
    private String ck, cs;

    private Twitter twitter;
    private RequestToken rt;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth);
        app = (App)getApplicationContext();

        TextView description = (TextView)findViewById(R.id.oauthDescription);
        String descri = "Custom CK/CSを使う場合、CallbackURLを<br><font color=blue><u>" + CALLBACK_URL + "</u></font><br>に設定してください。<br>（タップでコピー）";
        description.setText(Html.fromHtml(descri));

        customCK = (EditText)findViewById(R.id.edit_ck);
        customCS = (EditText)findViewById(R.id.edit_cs);

        ninsyobtn = (Button)findViewById(R.id.ninsyo);

        customCK.setText(app.getCurrentAccount().getConsumerKey());
        customCS.setText(app.getCurrentAccount().getConsumerSecret());
    }

    public void ninsyo(View v){
        ninsyobtn.setEnabled(false);
        if(customCK.getText().toString().isEmpty()){
            ck = getString(R.string.CK);
            cs = getString(R.string.CS);
        }else{
            ck = customCK.getText().toString();
            cs = customCS.getText().toString();
        }

        Configuration conf = new ConfigurationBuilder()
                .setOAuthConsumerKey(ck)
                .setOAuthConsumerSecret(cs)
                .build();
        new AsyncTask<Configuration, Void, Boolean>(){

            @Override
            protected Boolean doInBackground(Configuration... conf){
                twitter = new TwitterFactory(conf[0]).getInstance();
                try{
                    rt = twitter.getOAuthRequestToken(CALLBACK_URL);
                    return true;
                }catch(TwitterException e){
                    return false;
                }
            }

            @Override
            public void onPostExecute(Boolean result){
                if(result){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rt.getAuthenticationURL()));
                    startActivity(intent);
                }else{
                    new ShowToast(getApplicationContext(), R.string.error_get_request_token);
                }
            }
        }.execute(conf);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(intent == null || intent.getData() == null || !intent.getData().toString().startsWith(CALLBACK_URL)){
            return;
        }

        final String verifier = intent.getData().getQueryParameter("oauth_verifier");

        new AsyncTask<Void, Void, AccessToken>(){
            @Override
            protected AccessToken doInBackground(Void... params){
                try{
                    return twitter.getOAuthAccessToken(rt, verifier);
                }catch(Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(AccessToken accessToken){
                if(accessToken != null){
                    DBUtil dbUtil = app.getAccountDBUtil();
                    if(dbUtil.existsAccount(accessToken.getScreenName())){
                        String toast = getString(R.string.param_account_already_exists, accessToken.getScreenName());
                        new ShowToast(getApplicationContext(), toast, Toast.LENGTH_LONG);
                        finish();
                        return;
                    }
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putString(Keys.SCREEN_NAME, accessToken.getScreenName())
                            .commit();

                    if(ck.equals(getString(R.string.CK))){
                        ck = "";
                    }
                    if(cs.equals(getString(R.string.CS))){
                        cs = "";
                    }

                    Account account = new Account(accessToken.getScreenName(), ck, cs, accessToken.getToken(), accessToken.getTokenSecret());
                    dbUtil.addAcount(account);
                    app.resetAccount();
                    new ShowToast(getApplicationContext(), R.string.success_add_account);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }else{
                    new ShowToast(getApplicationContext(), R.string.error_get_access_token);
                }
                finish();
            }
        }.execute();
    }

    public void copyClipBoardDescription(View v){
        ClipboardManager clipboardManager = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Lightning of Dark", CALLBACK_URL);
        clipboardManager.setPrimaryClip(clipData);
        new ShowToast(getApplicationContext(), R.string.done_copy_clip_board);
    }

}
