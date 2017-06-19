package sugtao4423.lod;

import java.util.ArrayList;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
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

public class Settings_List extends PreferenceActivity{

	private Preference select_List, startApp_loadList;
	private SQLiteDatabase db;
	private ApplicationClass appClass;

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

			db = new SQLHelper(getActivity()).getWritableDatabase();

			appClass = (ApplicationClass)getActivity().getApplicationContext();

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
						db.execSQL("update accounts set showList='false' where screen_name = '" + appClass.getMyScreenName() + "'");
					}else{
						select_List.setEnabled(true);
						startApp_loadList.setEnabled(true);
						db.execSQL("update accounts set showList='true' where screen_name = '" + appClass.getMyScreenName() + "'");
					}
					return true;
				}
			});

			startApp_loadList.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference){
					Cursor c = db.rawQuery("select SelectListNames from accounts where screen_name=?",
							new String[]{appClass.getMyScreenName()});
					String c_str = null;
					while(c.moveToNext())
						c_str = c.getString(0);
					c.close();
					final String[] selectedListNames = c_str.split(",", 0);
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
									String startApp_loadLists = "";
									for(int i = 0; i < selectLoadList.size(); i++)
										startApp_loadLists += selectLoadList.get(i) + ",";
									SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
									pref.edit().putString("startApp_loadLists", startApp_loadLists).commit();
									db.execSQL("update accounts set startApp_loadLists='" + startApp_loadLists
											+ "' where screen_name='" + appClass.getMyScreenName() + "'");
									setSummary();
								}
							}).setNegativeButton("キャンセル", null);
					if(!selectedListNames[0].equals(""))
						builder.show();
					else
						new ShowToast("リストが選択されていません", getActivity(), 0);
					return false;
				}
			});

			select_List.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				ArrayList<String> array = new ArrayList<String>();

				@Override
				public boolean onPreferenceClick(Preference preference){
					new AsyncTask<Void, Void, ResponseList<UserList>>(){
						@Override
						protected ResponseList<UserList> doInBackground(Void... params){
							try{
								return appClass.getTwitter().getUserLists(appClass.getMyScreenName());
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

										SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
										pref.edit()
											.putInt("SelectListCount", checkedSize)
											.putString("SelectListIds", listIds)
											.putString("SelectListNames", listNames)
										.commit();
										db.execSQL("update accounts set SelectListCount='" + checkedSize
												+ "' where screen_name = '" + appClass.getMyScreenName() + "'");
										db.execSQL("update accounts set SelectListIds='" + listIds
												+ "' where screen_name = '" + appClass.getMyScreenName() + "'");
										db.execSQL("update accounts set SelectListNames='" + listNames
												+ "' where screen_name = '" + appClass.getMyScreenName() + "'");
										Dialog("リストを選択しました");
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

		public void Dialog(String title){
			new AlertDialog.Builder(getActivity())
			.setTitle(title)
			.setMessage("アプリを再起動してください。")
			.setPositiveButton("再起動", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			}).setNegativeButton("キャンセル", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					Toast.makeText(getActivity(), "大人しく再起動しような？", Toast.LENGTH_SHORT).show();
				}
			}).show();
		}

		public void setSummary(){
			Cursor nowSelectList = db.rawQuery("select SelectListNames from accounts where screen_name=?",
					new String[]{appClass.getMyScreenName()});
			Cursor nowStartAppLoadList = db.rawQuery("select startApp_loadLists from accounts where screen_name=?",
					new String[]{appClass.getMyScreenName()});
			nowSelectList.moveToNext();
			nowStartAppLoadList.moveToNext();
			String[] now1 = nowSelectList.getString(0).split(",", 0);
			String[] now2 = nowStartAppLoadList.getString(0).split(",", 0);
			nowSelectList.close();
			nowStartAppLoadList.close();
			String result1 = "設定値：", result2 = "設定値：";
			for(int i = 0; i < now1.length; i++)
				result1 += now1[i] + ", ";
			for(int i = 0; i < now2.length; i++)
				result2 += now2[i] + ", ";

			result1 = result1.substring(0, result1.length() - 2);
			result2 = result2.substring(0, result2.length() - 2);

			select_List.setSummary(result1);
			startApp_loadList.setSummary(result2);
		}
	}
}