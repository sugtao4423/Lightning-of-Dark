package sugtao4423.lod;

import java.util.ArrayList;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import sugtao4423.lod.utils.DBUtil;

public class Settings_List extends PreferenceActivity{

	private Preference select_List, startApp_loadList;
	private DBUtil dbUtil;
	private String myScreenName;
	private App app;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
		getActionBar().setTitle("リスト設定");
	}

	public class MyPreferencesFragment extends PreferenceFragment{
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_list);

			final CheckBoxPreference showList = (CheckBoxPreference)findPreference("showList");
			select_List = findPreference("select_List");
			startApp_loadList = findPreference("startApp_loadList");

			dbUtil = new DBUtil(getActivity());

			app = (App)getActivity().getApplicationContext();
			myScreenName = app.getCurrentAccount().getScreenName();

			setSummary();

			if(showList.isChecked()){
				select_List.setEnabled(true);
				startApp_loadList.setEnabled(true);
			}else{
				select_List.setEnabled(false);
				startApp_loadList.setEnabled(false);
			}

			showList.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue){
					if(showList.isChecked()){
						select_List.setEnabled(false);
						startApp_loadList.setEnabled(false);
						dbUtil.updateShowList(false, myScreenName);
					}else{
						select_List.setEnabled(true);
						startApp_loadList.setEnabled(true);
						dbUtil.updateShowList(true, myScreenName);
					}
					return true;
				}
			});

			startApp_loadList.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference){
					final String[] selectedListNames = dbUtil.getSelectListNames(myScreenName);
					boolean[] selectedLoadList = new boolean[selectedListNames.length];
					for(int i = 0; i < selectedListNames.length; i++)
						selectedLoadList[i] = false;
					final ArrayList<String> selectLoadList = new ArrayList<String>();
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
							.setTitle("起動時に読み込むリストを選択してください")
							.setMultiChoiceItems(selectedListNames, selectedLoadList, new OnMultiChoiceClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which, boolean isChecked){
									if(isChecked)
										selectLoadList.add(selectedListNames[which]);
									else
										selectLoadList.remove(selectedListNames[which]);
								}
							}).setPositiveButton("OK", new OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which){
									String appStartLoadLists = "";
									for(int i = 0; i < selectLoadList.size(); i++)
										appStartLoadLists += selectLoadList.get(i) + ",";
									dbUtil.updateStartAppLoadLists(appStartLoadLists, myScreenName);
									app.resetCurrentAccount();
									setSummary();
								}
							}).setNegativeButton("キャンセル", null);
					if(!selectedListNames[0].equals(""))
						builder.show();
					else
						new ShowToast(getApplicationContext(), R.string.listNotSelected);
					return false;
				}
			});

			select_List.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference){
					final ArrayList<String> array = new ArrayList<String>();
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
						protected void onPostExecute(final ResponseList<UserList> result){
							if(result != null){
								for(UserList userList : result)
									array.add(userList.getName());

								String[] listItem = (String[])array.toArray(new String[0]);
								boolean[] isCheck = new boolean[array.size()];
								for(int i = 0; i < array.size(); i++){
									isCheck[i] = false;
								}

								final ArrayList<UserList> checkedList = new ArrayList<UserList>();

								new AlertDialog.Builder(getActivity())
								.setTitle("リストを選択してください")
								.setMultiChoiceItems(listItem, isCheck, new OnMultiChoiceClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int which, boolean isChecked){
										if(isChecked)
											checkedList.add(result.get(which));
										else
											checkedList.remove(result.get(which));
									}
								}).setPositiveButton("OK", new OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int which){
										int checkedSize = checkedList.size();
										String listNames = "";
										String listIds = "";
										for(int i = 0; i < checkedSize; i++){
											listNames += checkedList.get(i).getName() + ",";
											listIds += checkedList.get(i).getId() + ",";
										}

										dbUtil.updateSelectListCount(checkedSize, myScreenName);
										dbUtil.updateSelectListIds(listIds, myScreenName);
										dbUtil.updateSelectListNames(listNames, myScreenName);
										app.resetCurrentAccount();
										setSummary();
									}
								}).show();
							}
						}
					}.execute();
					return false;
				}
			});
		}

		public void setSummary(){
			String[] nowSelectList = dbUtil.getSelectListNames(myScreenName);
			String[] nowStartAppLoadList = dbUtil.getNowStartAppLoadList(myScreenName);
			String result1 = "設定値：";
			String result2 = "設定値：";
			for(String s : nowSelectList)
				result1 += s + ", ";
			for(String s : nowStartAppLoadList)
				result2 += s + ", ";

			result1 = result1.substring(0, result1.length() - 2);
			result2 = result2.substring(0, result2.length() - 2);

			select_List.setSummary(result1);
			startApp_loadList.setSummary(result2);
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