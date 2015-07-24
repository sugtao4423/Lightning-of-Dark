package com.tao.lightning_of_dark;

import java.util.ArrayList;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
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
import android.widget.Toast;

public class Preference_List extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content,  new MyPreferencesFragment()).commit();
		ActionBar actionbar = getActionBar();
		actionbar.setHomeButtonEnabled(true);
	}
	
	public class MyPreferencesFragment extends PreferenceFragment {
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
				
				ArrayList<String> array = new ArrayList<String>();
				ResponseList<UserList> lists;
				
				@Override
				public boolean onPreferenceClick(android.preference.Preference preference) {
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
						@Override
						protected void onPostExecute(Boolean result) {
							if(result){
								for(UserList userList : lists)
									array.add(userList.getName());
								
								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
								builder.setTitle("リストを選択してください");
								String[] listItem = (String[])array.toArray(new String[0]);
								boolean[] isCheck = new boolean[array.size()];
								for(int i = 0; i < array.size(); i++)
									isCheck[i] = false;
								
								final ArrayList<UserList> checkedList = new ArrayList<UserList>();
								builder.setMultiChoiceItems(listItem, isCheck, new OnMultiChoiceClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which, boolean isChecked) {
										if(isChecked)
											checkedList.add(lists.get(which));
										else
											checkedList.remove(lists.get(which));
									}
								});
								builder.setPositiveButton("OK", new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										int checkedSize = checkedList.size();
										String listNames = "";
										String listIds = "";
										for(int i = 0; i < checkedSize; i++){
											listNames += checkedList.get(i).getName() + ",";
											listIds += checkedList.get(i).getId() + ",";
										}
										
										SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
										Editor edit = pref.edit()
										.putInt("SelectListCount", checkedSize)
										.putString("SelectListIds", listIds)
										.putString("SelectListNames", listNames);
										db.execSQL("update accounts set SelectListCount='" + checkedSize + "' where screen_name = '" + appClass.getMyScreenName() + "'");
										db.execSQL("update accounts set SelectListIds='" + listIds + "' where screen_name = '" + appClass.getMyScreenName() + "'");
										db.execSQL("update accounts set SelectListNames='" + listNames + "' where screen_name = '" + appClass.getMyScreenName() + "'");
										if(edit.commit()){
											Dialog("リストを選択しました");
										}else{
											Toast.makeText(getActivity(), "リストを選択できませんでした", Toast.LENGTH_SHORT).show();
										}
									}
								});
								builder.create().show();
							}
						}
					};
					task.execute();
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