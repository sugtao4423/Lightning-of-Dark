package sugtao4423.lod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.text.NumberFormat;
import java.util.ArrayList;

import sugtao4423.lod.dataclass.Account;
import sugtao4423.lod.userpage_fragment.UserPage;
import sugtao4423.lod.usetime.UseTime;
import sugtao4423.lod.utils.DBUtil;
import sugtao4423.lod.utils.Utils;
import twitter4j.TwitterException;

public class OptionClickListener implements OnClickListener{

    private Context context;

    public OptionClickListener(Context context){
        this.context = context;
    }

    @Override
    public void onClick(DialogInterface dialog, int which){
        switch(which){
            case 0:
                tweetBomb();
                break;
            case 1:
                searchUser();
                break;
            case 2:
                accountSelect();
                break;
            case 3:
                levelInfo();
                break;
            case 4:
                useInfo();
                break;
            case 5:
                context.startActivity(new Intent(context, Settings.class));
                break;
        }
    }

    public void tweetBomb(){
        final View bombView = View.inflate(context, R.layout.tweet_bomb, null);
        new AlertDialog.Builder(context)
                .setView(bombView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        final String staticText = ((EditText)bombView.findViewById(R.id.bomb_staticText)).getText().toString();
                        final String loopText = ((EditText)bombView.findViewById(R.id.bomb_loopText)).getText().toString();
                        int loopCount = Integer.parseInt(((EditText)bombView.findViewById(R.id.bomb_loopCount)).getText().toString());

                        String loop = "";
                        for(int i = 0; i < loopCount; i++){
                            loop += loopText;
                            new AsyncTask<String, Void, Void>(){
                                @Override
                                protected Void doInBackground(String... params){
                                    try{
                                        ((App)context.getApplicationContext()).getTwitter().updateStatus(staticText + params[0]);
                                    }catch(TwitterException e){
                                    }
                                    return null;
                                }
                            }.execute(loop);
                        }
                        String toast = context.getString(R.string.param_success_tweet, 0);
                        new ShowToast(context.getApplicationContext(), toast);
                    }
                }).show();
    }

    public void searchUser(){
        final EditText userEdit = new EditText(context);
        FrameLayout editContainer = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = Utils.convertDpToPx(context, 24);
        params.leftMargin = margin;
        params.rightMargin = margin;
        userEdit.setLayoutParams(params);
        editContainer.addView(userEdit);

        new AlertDialog.Builder(context)
                .setMessage(R.string.input_users_screen_name)
                .setView(editContainer)
                .setPositiveButton(R.string.ok, new OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String user_screen = userEdit.getText().toString();
                        if(user_screen.isEmpty()){
                            new ShowToast(context.getApplicationContext(), R.string.edittext_empty);
                        }else{
                            Intent userPage = new Intent(context, UserPage.class);
                            userPage.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, user_screen.replace("@", ""));
                            context.startActivity(userPage);
                        }
                    }
                }).show();
    }

    public void accountSelect(){
        App app = (App)context.getApplicationContext();
        final String myScreenName = app.getCurrentAccount().getScreenName();
        final DBUtil dbUtil = app.getAccountDBUtil();
        final Account[] accounts = dbUtil.getAccounts();
        ArrayList<String> screenNames = new ArrayList<String>();
        for(Account acc : accounts){
            if(acc.getScreenName().equals(myScreenName)){
                screenNames.add("@" + acc.getScreenName() + " (now)");
            }else{
                screenNames.add("@" + acc.getScreenName());
            }
        }
        screenNames.add(context.getString(R.string.add_account));
        final String[] nameDialog = (String[])screenNames.toArray(new String[0]);
        new AlertDialog.Builder(context)
                .setItems(nameDialog, new OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, final int which){
                        if(nameDialog[which].equals(context.getString(R.string.add_account))){
                            context.startActivity(new Intent(context, StartOAuth.class));
                        }else if(!nameDialog[which].equals("@" + myScreenName + " (now)")){
                            new AlertDialog.Builder(context)
                                    .setTitle(nameDialog[which])
                                    .setPositiveButton(R.string.change_account, new OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int w){
                                            PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                                                    .edit()
                                                    .putString(Keys.SCREEN_NAME, accounts[which].getScreenName())
                                                    .commit();
                                            ((MainActivity)context).restart();
                                        }
                                    }).setNegativeButton(R.string.delete, new OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int w){
                                    dbUtil.deleteAccount(accounts[which]);
                                    String toast = context.getString(R.string.param_success_account_delete, accounts[which].getScreenName());
                                    new ShowToast(context.getApplicationContext(), toast);
                                }
                            }).setNeutralButton(R.string.cancel, null).show();
                        }
                    }
                }).show();
    }

    public void levelInfo(){
        Level lv = ((App)context.getApplicationContext()).getLevel();
        NumberFormat nf = NumberFormat.getInstance();
        String level = nf.format(lv.getLevel());
        String nextExp = nf.format(lv.getNextExp());
        String totalExp = nf.format(lv.getTotalExp());
        String message = context.getString(R.string.param_next_level_total_exp, level, nextExp, totalExp);
        new AlertDialog.Builder(context).setMessage(message).setPositiveButton(R.string.ok, null).show();
    }

    public void useInfo(){
        UseTime useTime = ((App)context.getApplicationContext()).getUseTime();
        int todayUse = useTime.getTodayUseTimeInMillis();
        int yesterdayUse = useTime.getYesterdayUseTimeInMillis();
        long last30daysUse = useTime.getLastNdaysUseTimeInMillis(30);
        long totalUse = useTime.getTotalUseTimeInMillis();
        String message = context.getString(R.string.param_use_info_text,
                milliTime2Str(todayUse), milliTime2Str(yesterdayUse), milliTime2Str(last30daysUse), milliTime2Str(totalUse));
        new AlertDialog.Builder(context).setTitle(R.string.use_info).setMessage(message).setPositiveButton(R.string.ok, null).show();
    }

    private String milliTime2Str(long time){
        int day = (int)time / 1000 / 86400;
        int hour = (int)(time / 1000 - day * 86400) / 3600;
        int minute = (int)(time / 1000 - day * 86400 - hour * 3600) / 60;
        int second = (int)time / 1000 - day * 86400 - hour * 3600 - minute * 60;

        String result = "";
        if(day != 0){
            result += day + " days, ";
        }
        result += zeroPad(hour) + ":" + zeroPad(minute) + ":" + zeroPad(second);
        return result;
    }

    private String zeroPad(int i){
        if(i < 10){
            return "0" + i;
        }else{
            return String.valueOf(i);
        }
    }

}