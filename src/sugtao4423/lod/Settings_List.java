package sugtao4423.lod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import sugtao4423.lod.utils.DBUtil;
import sugtao4423.lod.utils.Utils;

public class Settings_List extends PreferenceActivity{

	private Preference select_List, startApp_loadList;
	private DBUtil dbUtil;
	private String myScreenName;
	private App app;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
		getActionBar().setTitle("リスト設定 (要再起動)");
	}

	public class MyPreferencesFragment extends PreferenceFragment{
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_list);

			select_List = findPreference("select_List");
			startApp_loadList = findPreference("startApp_loadList");

			app = (App)getActivity().getApplicationContext();
			dbUtil = app.getAccountDBUtil();
			myScreenName = app.getCurrentAccount().getScreenName();

			setSummary();

			startApp_loadList.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference){
					final String[] selectedListNames = dbUtil.getSelectListNames(myScreenName);
					boolean[] selectedLoadList = new boolean[selectedListNames.length];
					final ArrayList<String> selectLoadList = new ArrayList<String>();
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
					.setTitle("起動時に読み込むリストを選択してください")
					.setMultiChoiceItems(selectedListNames, selectedLoadList, new OnMultiChoiceClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked){
							if(isChecked){
								selectLoadList.add(selectedListNames[which]);
							}else{
								selectLoadList.remove(selectedListNames[which]);
							}
						}
					}).setPositiveButton("OK", new OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which){
							dbUtil.updateStartAppLoadLists(Utils.implode(selectLoadList), myScreenName);
							app.resetAccount();
							setSummary();
						}
					}).setNegativeButton("キャンセル", null);
					if(!selectedListNames[0].equals("")){
						builder.show();
					}else{
						new ShowToast(getApplicationContext(), R.string.listNotSelected);
					}
					return false;
				}
			});

			select_List.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference){
					new AsyncTask<Void, Void, ResponseList<UserList>>(){
						@Override
						protected ResponseList<UserList> doInBackground(Void... params){
							try{
								return app.getTwitter().getUserLists(myScreenName);
							}catch(TwitterException e){
								return null;
							}
						}

						@Override
						protected void onPostExecute(ResponseList<UserList> result){
							if(result == null){
								new ShowToast(Settings_List.this, R.string.error_getList);
								return;
							}
							final HashMap<String, Long> listMap = new HashMap<String, Long>();
							for(UserList l : result){
								listMap.put(l.getName(), l.getId());
							}

							final String[] listItem = (String[])listMap.keySet().toArray(new String[0]);
							final LinkedHashMap<String, Long> checkedList = new LinkedHashMap<String, Long>();

							new AlertDialog.Builder(getActivity())
							.setTitle("リストを選択してください")
							.setMultiChoiceItems(listItem, new boolean[listItem.length], new OnMultiChoiceClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which, boolean isChecked){
									if(isChecked){
										checkedList.put(listItem[which], listMap.get(listItem[which]));
									}else{
										checkedList.remove(listItem[which]);
									}
								}
							}).setPositiveButton("OK", new OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which){
									String[] checkedListNames = checkedList.keySet().toArray(new String[0]);
									Long[] checkedListIds = checkedList.values().toArray(new Long[0]);

									dbUtil.updateSelectListNames(Utils.implode(checkedListNames), myScreenName);
									dbUtil.updateSelectListIds(Utils.implode(checkedListIds), myScreenName);
									app.resetAccount();
									setSummary();
								}
							}).show();
						}
					}.execute();
					return false;
				}
			});
		}

		public void setSummary(){
			String[] selectList = dbUtil.getSelectListNames(myScreenName);
			String[] startAppLoadList = dbUtil.getNowStartAppLoadList(myScreenName);
			String summary1 = "設定値: " + Utils.implode(selectList);
			String summary2 = "設定値: " + Utils.implode(startAppLoadList);
			select_List.setSummary(summary1);
			startApp_loadList.setSummary(summary2);
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		app.getUseTime().start();
	}

	@Override
	public void onPause(){
		super.onPause();
		app.getUseTime().stop();
	}

}