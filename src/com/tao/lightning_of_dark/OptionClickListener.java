package com.tao.lightning_of_dark;

import java.util.ArrayList;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.dataclass.Account;
import com.tao.lightning_of_dark.userPageFragment.UserPage;
import com.tao.lightning_of_dark.utils.DBUtil;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

public class OptionClickListener implements OnClickListener{

	private Context context;
	private String[] items;
	private SharedPreferences pref;
	private Twitter twitter;

	public OptionClickListener(Context context, String[] items, SharedPreferences pref, Twitter twitter){
		this.context = context;
		this.items = items;
		this.pref = pref;
		this.twitter = twitter;
	}

	@SuppressLint("InflateParams")
	@Override
	public void onClick(DialogInterface dialog, int which){
		if(items[which].equals("ユーザー検索")){
			final EditText userEdit = new EditText(context);
			new AlertDialog.Builder(context)
			.setMessage("ユーザーのスクリーンネームを入力してください")
			.setView(userEdit)
			.setPositiveButton("OK", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					Intent userPage = new Intent(context, UserPage.class);
					String user_screen = userEdit.getText().toString();
					if(user_screen.isEmpty()){
						new ShowToast("なにも入力されていません", context, 0);
					}else{
						userPage.putExtra("userScreenName", user_screen.replace("@", ""));
						context.startActivity(userPage);
					}
				}
			}).show();
		}
		if(items[which].equals("アカウント")){
			final String myScreenName = pref.getString("ScreenName", "");
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
								pref.edit().putString("ScreenName", accounts[which].getScreenName())
									.putString("CustomCK", accounts[which].getCK())
									.putString("CustomCS", accounts[which].getCS())
									.putString("AccessToken", accounts[which].getAT())
									.putString("AccessTokenSecret", accounts[which].getATS())
									.putBoolean("showList", accounts[which].getShowList())
									.putInt("SelectListCount", accounts[which].getSelectListCount())
									.putString("SelectListIds", accounts[which].getSelectListIds())
									.putString("SelectListNames", accounts[which].getSelectListNames())
									.putString("startApp_loadLists", accounts[which].getStartAppLoadLists())
								.commit();
								((MainActivity)context).restart();
							}
						}).setNegativeButton("削除", new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog, int w){
								dbUtil.deleteAccount(accounts[which]);
								new ShowToast("@" + accounts[which].getScreenName() + "を削除しました", context, 0);
							}
						}).setNeutralButton("キャンセル", null).show();
					}
				}
			}).show();
		}
		if(items[which].equals("設定")){
			context.startActivity(new Intent(context, Settings.class));
		}
		if(items[which].equals("ツイート爆撃")){
			final View bombView = ((MainActivity)context).getLayoutInflater().inflate(R.layout.tweet_bomb, null);
			new AlertDialog.Builder(context)
			.setView(bombView)
			.setPositiveButton("OK", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					EditText _staticText = (EditText)bombView.findViewById(R.id.bomb_staticText);
					EditText _loopText = (EditText)bombView.findViewById(R.id.bomb_loopText);
					EditText _loopCount = (EditText)bombView.findViewById(R.id.bomb_loopCount);

					final String staticText = _staticText.getText().toString();
					final String loopText = _loopText.getText().toString();
					int loopCount = Integer.parseInt(_loopCount.getText().toString());

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
					new ShowToast("ツイート完了", context, 0);
				}
			}).setNegativeButton("キャンセル", null).show();
		}
	}
}