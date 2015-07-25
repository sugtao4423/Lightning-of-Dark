package com.tao.lightning_of_dark;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

public class OptionClickListener implements OnClickListener {

	private Context context;
	private String[] items;
	private String myScreenName;
	private SharedPreferences pref;
	private Twitter twitter;
	
	public OptionClickListener(Context context, String[] items, String myScreenName, SharedPreferences pref, Twitter twitter) {
		this.context = context;
		this.items = items;
		this.myScreenName = myScreenName;
		this.pref = pref;
		this.twitter = twitter;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(items[which].equals("ユーザー検索")){
			AlertDialog.Builder userSearch = new AlertDialog.Builder(((MainActivity)context));
			final EditText userEdit = new EditText(((MainActivity)context));
			userSearch.setMessage("ユーザーのスクリーンネームを入力してください")
			.setView(userEdit)
			.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent userPage = new Intent(((MainActivity)context), UserPage.class);
					String user_screen = userEdit.getText().toString();
					if(user_screen.isEmpty())
						new ShowToast("なにも入力されていません", ((MainActivity)context), 0);
					else{
						userPage.putExtra("userScreenName", user_screen.replace("@", ""));
						((MainActivity)context).startActivity(userPage);
					}
				}
			});
			userSearch.create().show();
		}
		if(items[which].equals("アカウント")){
			SQLiteDatabase db = new SQLHelper(((MainActivity)context)).getWritableDatabase();
			String[] columns = new String[]{"screen_name", "CK", "CS", "AT", "ATS", "showList", "SelectListCount", "SelectListIds", "SelectListNames", "startApp_loadLists"};
			Cursor result = db.query("accounts", columns, null, null, null, null, null);
			boolean mov = result.moveToFirst();
			List<String> selectAccount_screenName = new ArrayList<String>();
			final List<String> selectAccount_CK = new ArrayList<String>();
			final List<String> selectAccount_CS = new ArrayList<String>();
			final List<String> selectAccount_AT = new ArrayList<String>();
			final List<String> selectAccount_ATS = new ArrayList<String>();
			final List<Boolean> selectAccount_showList = new ArrayList<Boolean>();
			final List<Integer> selectAccount_SelectListCount = new ArrayList<Integer>();
			final List<String> selectAccount_SelectListIds = new ArrayList<String>();
			final List<String> selectAccount_SelectListNames = new ArrayList<String>();
			final List<String> selectAccount_startApp_loadLists = new ArrayList<String>();
			while(mov){
				String screen = "@" + result.getString(0);
				if(screen.equals("@" + myScreenName))
					screen = screen + " (now)";
				selectAccount_screenName.add(screen);
				selectAccount_CK.add(result.getString(1));
				selectAccount_CS.add(result.getString(2));
				selectAccount_AT.add(result.getString(3));
				selectAccount_ATS.add(result.getString(4));
				selectAccount_showList.add(Boolean.parseBoolean(result.getString(5)));
				selectAccount_SelectListCount.add(Integer.parseInt(result.getString(6)));
				selectAccount_SelectListIds.add(result.getString(7));
				selectAccount_SelectListNames.add(result.getString(8));
				selectAccount_startApp_loadLists.add(result.getString(9));
				
				mov = result.moveToNext();
			}
			selectAccount_screenName.add("アカウントを追加");
			AlertDialog.Builder screennameDialog = new AlertDialog.Builder(((MainActivity)context));
			final String[] nameDialog = (String[])selectAccount_screenName.toArray(new String[0]);
			screennameDialog.setItems(nameDialog, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(nameDialog[which].equals("アカウントを追加")){
						((MainActivity)context).startActivity(new Intent(((MainActivity)context), startOAuth.class));
					}else if(!nameDialog[which].equals("@" + myScreenName + " (now)")){
						pref.edit()
						.putString("CustomCK", selectAccount_CK.get(which))
						.putString("CustomCS", selectAccount_CS.get(which))
						.putString("AccessToken", selectAccount_AT.get(which))
						.putString("AccessTokenSecret", selectAccount_ATS.get(which))
						.putBoolean("showList", selectAccount_showList.get(which))
						.putInt("SelectListCount", selectAccount_SelectListCount.get(which))
						.putString("SelectListIds", selectAccount_SelectListIds.get(which))
						.putString("SelectListNames", selectAccount_SelectListNames.get(which))
						.putString("startApp_loadLists", selectAccount_startApp_loadLists.get(which)).commit();
						((MainActivity)context).restart();
					}
				}
			});
			screennameDialog.create().show();
		}
		if(items[which].equals("設定")){
			((MainActivity)context).startActivity(new Intent(((MainActivity)context), Preference.class));
		}
		if(items[which].equals("ツイート爆撃")){
			AlertDialog.Builder builder = new AlertDialog.Builder(((MainActivity)context));
			final View bombView = ((MainActivity)context).getLayoutInflater().inflate(R.layout.tweet_bomb, null);
			builder.setView(bombView);
			builder.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText _staticText = (EditText)bombView.findViewById(R.id.bomb_staticText);
					EditText _loopText = (EditText)bombView.findViewById(R.id.bomb_loopText);
					EditText _loopCount = (EditText)bombView.findViewById(R.id.bomb_loopCount);
					
					final String staticText = _staticText.getText().toString();
					final String loopText = _loopText.getText().toString();
					int loopCount = Integer.parseInt(_loopCount.getText().toString());
					
					String loop = "";
					for(int i = 0; i < loopCount; i++){
						loop += loopText;
						AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>(){
							@Override
							protected Void doInBackground(String... params) {
								try {
									twitter.updateStatus(staticText + params[0]);
								} catch (TwitterException e) {}
								return null;
							}
						};
						task.execute(loop);
					}
					new ShowToast("ツイート完了", ((MainActivity)context), 0);
				}
			});
			builder.setNegativeButton("キャンセル", null);
			builder.create().show();
		}
	}
}