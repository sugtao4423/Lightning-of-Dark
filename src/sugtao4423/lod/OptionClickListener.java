package sugtao4423.lod;

import java.util.ArrayList;
import java.util.Locale;

import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import sugtao4423.lod.dataclass.Account;
import sugtao4423.lod.userpage_fragment.UserPage;
import sugtao4423.lod.utils.DBUtil;

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
			refreshHomeLine();
			break;
		case 3:
			accountSelect();
			break;
		case 4:
			levelInfo();
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
		.setPositiveButton("OK", new OnClickListener(){
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
								((ApplicationClass)context.getApplicationContext()).getTwitter().updateStatus(staticText + params[0]);
							}catch(TwitterException e){
							}
							return null;
						}
					}.execute(loop);
				}
				new ShowToast(context.getApplicationContext(), R.string.success_tweet);
			}
		}).setNegativeButton("キャンセル", null).show();
	}

	public void searchUser(){
		final EditText userEdit = new EditText(context);
		new AlertDialog.Builder(context)
		.setMessage("ユーザーのスクリーンネームを入力してください")
		.setView(userEdit)
		.setPositiveButton("OK", new OnClickListener(){
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

	public void refreshHomeLine(){
		new AlertDialog.Builder(context)
		.setTitle("Homeを更新しますか？")
		.setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				((MainActivity)context).getTimeLine();
			}
		}).setNegativeButton("キャンセル", null).show();
	}

	public void accountSelect(){
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final String myScreenName = pref.getString(Keys.SCREEN_NAME, "");
		final DBUtil dbUtil = new DBUtil(context);
		final Account[] accounts = dbUtil.getAccounts();
		ArrayList<String> screen_names = new ArrayList<String>();
		for(Account acc : accounts){
			if(acc.getScreenName().equals(myScreenName))
				screen_names.add("@" + acc.getScreenName() + " (now)");
			else
				screen_names.add("@" + acc.getScreenName());
		}
		screen_names.add("アカウントを追加");
		final String[] nameDialog = (String[])screen_names.toArray(new String[0]);
		new AlertDialog.Builder(context)
		.setItems(nameDialog, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, final int which){
				if(nameDialog[which].equals("アカウントを追加")){
					context.startActivity(new Intent(context, StartOAuth.class));
				}else if(!nameDialog[which].equals("@" + myScreenName + " (now)")){
					new AlertDialog.Builder(context)
					.setTitle(nameDialog[which])
					.setPositiveButton("切り替え", new OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int w){
							pref.edit()
								.putString(Keys.SCREEN_NAME, accounts[which].getScreenName())
								.putString(Keys.CUSTOM_CK, accounts[which].getCK())
								.putString(Keys.CUSTOM_CS, accounts[which].getCS())
								.putString(Keys.ACCESS_TOKEN, accounts[which].getAT())
								.putString(Keys.ACCESS_TOKEN_SECRET, accounts[which].getATS())
								.putBoolean(Keys.SHOW_LIST, accounts[which].getShowList())
								.putInt(Keys.SELECT_LIST_COUNT, accounts[which].getSelectListCount())
								.putString(Keys.SELECT_LIST_IDS, accounts[which].getSelectListIds())
								.putString(Keys.SELECT_LIST_NAMES, accounts[which].getSelectListNames())
								.putString(Keys.APP_START_LOAD_LISTS, accounts[which].getStartAppLoadLists())
							.commit();
							((MainActivity)context).restart();
						}
					}).setNegativeButton("削除", new OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int w){
							dbUtil.deleteAccount(accounts[which]);
							new ShowToast(context.getApplicationContext(), String.format(context.getString(R.string.deleteAccountX), accounts[which].getScreenName()));
						}
					}).setNeutralButton("キャンセル", null).show();
				}
			}
		}).show();
	}

	public void levelInfo(){
		Level lv = ((ApplicationClass)context.getApplicationContext()).getLevel();
		int level = lv.getLevel();
		int nextExp = lv.getNextExp();
		int totalExp = lv.getTotalExp();
		String message = String.format(Locale.JAPAN, "Lv.%d\nレベルアップまで: %dEXP\n取得経験値: %dEXP", level, nextExp, totalExp);
		new AlertDialog.Builder(context).setMessage(message).setPositiveButton("OK", null).show();
	}

}