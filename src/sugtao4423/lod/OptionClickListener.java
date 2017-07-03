package sugtao4423.lod;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import sugtao4423.lod.dataclass.Account;
import sugtao4423.lod.userpage_fragment.UserPage;
import sugtao4423.lod.utils.DBUtil;

public class OptionClickListener implements OnClickListener{

	private MainActivity context;
	private String[] items;
	private SharedPreferences pref;
	private Twitter twitter;

	public OptionClickListener(MainActivity context, String[] items, SharedPreferences pref, Twitter twitter){
		this.context = context;
		this.items = items;
		this.pref = pref;
		this.twitter = twitter;
	}

	@Override
	public void onClick(DialogInterface dialog, int which){
		switch(items[which]){
		case "ユーザー検索":
			searchUser();
			break;
		case "アカウント":
			accountSelect();
			break;
		case "設定":
			context.startActivity(new Intent(context, Settings.class));
			break;
		case "ツイート爆撃":
			tweetBomb();
			break;
		case "Homeを更新":
			refreshHomeLine();
			break;
		}
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
					new ShowToast(R.string.edittext_empty, context, 0);
				}else{
					Intent userPage = new Intent(context, UserPage.class);
					userPage.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, user_screen.replace("@", ""));
					context.startActivity(userPage);
				}
			}
		}).show();
	}

	public void accountSelect(){
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
							pref.edit().putString(Keys.SCREEN_NAME, accounts[which].getScreenName())
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
							new ShowToast(String.format(context.getString(R.string.deleteAccountX), accounts[which].getScreenName()), context, 0);
						}
					}).setNeutralButton("キャンセル", null).show();
				}
			}
		}).show();
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
								twitter.updateStatus(staticText + params[0]);
							}catch(TwitterException e){
							}
							return null;
						}
					}.execute(loop);
				}
				new ShowToast(R.string.success_tweet, context, 0);
			}
		}).setNegativeButton("キャンセル", null).show();
	}

	public void refreshHomeLine(){
		new AlertDialog.Builder(context)
		.setTitle("Homeを更新しますか？")
		.setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				context.getTimeLine();
			}
		}).setNegativeButton("キャンセル", null).show();
	}
}