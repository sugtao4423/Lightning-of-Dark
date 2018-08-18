package sugtao4423.lod;

import java.text.DecimalFormat;
import java.util.HashMap;

import com.loopj.android.image.WebImageCache;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;
import sugtao4423.lod.utils.DBUtil;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Settings extends PreferenceActivity{

	private App app;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		app = (App)getApplicationContext();
		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
		getActionBar().setTitle("設定");
	}

	public class MyPreferencesFragment extends PreferenceFragment{
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);

			CheckBoxPreference listAsTL = (CheckBoxPreference)findPreference("listAsTL");
			Preference listSetting = findPreference("listSetting");
			Preference clearCache = findPreference("clearCache");
			setCacheSize(clearCache);

			listAsTL.setChecked(app.getCurrentAccount().getListAsTL() > 0);
			listAsTL.setSummary(app.getCurrentAccount().getListAsTL() > 0 ? String.valueOf(app.getCurrentAccount().getListAsTL()) : null);
			listAsTL.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue){
					boolean isCheck = Boolean.parseBoolean(newValue.toString());
					return selectListAsTL(preference, isCheck);
				}
			});

			listSetting.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference){
					Intent intent = new Intent(getActivity(), Settings_List.class);
					startActivity(intent);
					return false;
				}
			});

			clearCache.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference){
					new WebImageCache(Settings.this).clear();
					setCacheSize(preference);
					Toast.makeText(Settings.this, "キャッシュが削除されました", Toast.LENGTH_SHORT).show();
					return false;
				}
			});
		}
	}

	public boolean selectListAsTL(final Preference preference, boolean isCheck){
		final DBUtil dbutil = new DBUtil(getApplicationContext());
		if(!isCheck){
			new AlertDialog.Builder(Settings.this)
			.setTitle("解除しますか？")
			.setPositiveButton("OK", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					dbutil.updateListAsTL(-1, app.getCurrentAccount().getScreenName());
					preference.setSummary(null);
				}
			}).setNegativeButton("Cancel", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					((CheckBoxPreference)preference).setChecked(true);
				}
			}).show();
			return true;
		}

		final HashMap<String, Long> listMap = new HashMap<String, Long>();
		new AsyncTask<Void, Void, ResponseList<UserList>>(){
			@Override
			protected ResponseList<UserList> doInBackground(Void... params){
				try{
					return app.getTwitter().getUserLists(app.getTwitter().getScreenName());
				}catch(IllegalStateException | TwitterException e){
					return null;
				}
			}
			@Override
			protected void onPostExecute(ResponseList<UserList> result){
				if(result == null){
					new ShowToast(getApplicationContext(), R.string.error_getList);
					return;
				}
				for(UserList l : result){
					listMap.put(l.getName(), l.getId());
				}
				final String[] listNames = (String[])listMap.keySet().toArray(new String[0]);
				new AlertDialog.Builder(Settings.this)
				.setTitle("TLとして読み込むリストを選択してください")
				.setCancelable(false)
				.setItems(listNames, new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						long selectedListId = listMap.get(listNames[which]);
						dbutil.updateListAsTL(selectedListId, app.getCurrentAccount().getScreenName());
						preference.setSummary(String.valueOf(selectedListId));
					}
				}).show();
			}
		}.execute();
		return true;
	}

	public void setCacheSize(final Preference clearCache){
		new AsyncTask<Void, Void, String>(){
			@Override
			protected String doInBackground(Void... params){
				DecimalFormat df = new DecimalFormat("#.#");
				df.setMinimumFractionDigits(2);
				df.setMaximumFractionDigits(2);
				return df.format((double)new WebImageCache(Settings.this).getCacheSize() / 1024 / 1024) + "MB";
			}
			@Override
			protected void onPostExecute(String result){
				clearCache.setSummary("キャッシュ: " + result);
			}
		}.execute();
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

	@Override
	public void onDestroy(){
		super.onDestroy();
		app.loadOption();
		app.reloadAccountFromDB();
	}

}
