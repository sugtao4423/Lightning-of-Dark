package com.tao.lightning_of_dark;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Preference_List extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content,  new MyPreferencesFragment()).commit();
		ActionBar actionbar = getActionBar();
		actionbar.setHomeButtonEnabled(true);
	}
	
	public static class MyPreferencesFragment extends PreferenceFragment {
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_list);
			
			final CheckBoxPreference showList = (CheckBoxPreference) findPreference("showList");
			final android.preference.Preference select_List = findPreference("select_List");
			final CheckBoxPreference startApp_showList = (CheckBoxPreference)findPreference("startApp_showList");
			
			final SQLiteDatabase db = new SQLHelper(getActivity()).getWritableDatabase();
			
			final ApplicationClass appClass = (ApplicationClass)getActivity().getApplicationContext();;
			
			if(showList.isChecked()){
				select_List.setEnabled(true);
				startApp_showList.setEnabled(true);
			}else{
				select_List.setEnabled(false);
				startApp_showList.setEnabled(false);
			}
			
			showList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
					if(showList.isChecked()){
						select_List.setEnabled(false);
						startApp_showList.setEnabled(false);
						db.execSQL("update accounts set showList='false' where screen_name = '" + appClass.getMyScreenName() + "'");
					}else{
						select_List.setEnabled(true);
						startApp_showList.setEnabled(true);
						db.execSQL("update accounts set showList='true' where screen_name = '" + appClass.getMyScreenName() + "'");
					}
					return true;
				}
			});
			startApp_showList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if(startApp_showList.isChecked()){
						db.execSQL("update accounts set startApp_showList='false' where screen_name = '" + appClass.getMyScreenName() + "'");
					}else{
						db.execSQL("update accounts set startApp_showList='true' where screen_name = '" + appClass.getMyScreenName() + "'");
					}
					return true;
				}
			});
			
			select_List.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				ListView ListNameList;
				ArrayAdapter<String> array;
				ResponseList<UserList> lists;
				
				@Override
				public boolean onPreferenceClick(android.preference.Preference preference) {
					ListNameList = new ListView(getActivity());
					array = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
					ListNameList.setAdapter(array);
					AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								lists = appClass.getTwitter().getUserLists(appClass.getMyScreenName());
								return true;
							} catch (TwitterException e) {
								return false;
							}
						}
						protected void onPostExecute(Boolean result) {
							if(result){
								for(UserList userList : lists)
									array.add(userList.getName());
							}
						}
					};
					task.execute();
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setView(ListNameList).setTitle("リストを選択してください");
					builder.create().show();
					ListNameList.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							long ListId = lists.get(position).getId();
							String ListName = lists.get(position).getName();
							SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
							Editor edit = pref.edit()
							.putLong("SelectListId", ListId)
							.putString("SelectListName", ListName);
							db.execSQL("update accounts set SelectListId='" + ListId + "' where screen_name = '" + appClass.getMyScreenName() + "'");
							db.execSQL("update accounts set SelectListName='" + ListName + "' where screen_name = '" + appClass.getMyScreenName() + "'");
							if(edit.commit()){
								Dialog("リストを選択しました");
							}else
								Toast.makeText(getActivity(), "リストを選択できませんでした", Toast.LENGTH_SHORT).show();
						}
					});
					return false;
				}
			});
		}
		public void Dialog(String title){
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle(title)
			.setMessage("アプリを再起動してください。")
			.setPositiveButton("再起動", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			});
			dialog.setNegativeButton("キャンセル", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(getActivity(), "大人しく再起動しような？", Toast.LENGTH_SHORT).show();
				}
			});
			dialog.create().show();
		}
	}
}
