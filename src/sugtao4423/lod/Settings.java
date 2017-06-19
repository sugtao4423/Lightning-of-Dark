package sugtao4423.lod;

import java.text.DecimalFormat;

import com.loopj.android.image.WebImageCache;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Settings extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
		getActionBar().setTitle("設定");
	}

	public class MyPreferencesFragment extends PreferenceFragment{
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);

			Preference ListSetting = findPreference("ListSetting");
			Preference clearCache = findPreference("clearCache");
			clearCache.setSummary("キャッシュ: " + getCacheSize());

			ListSetting.setOnPreferenceClickListener(new OnPreferenceClickListener(){
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
					preference.setSummary("キャッシュ: " + getCacheSize());
					Toast.makeText(Settings.this, "キャッシュが削除されました", Toast.LENGTH_SHORT).show();
					return false;
				}
			});
		}
	}

	public String getCacheSize(){
		DecimalFormat df = new DecimalFormat("#.#");
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		return df.format((double)new WebImageCache(Settings.this).getCacheSize() / 1024 / 1024) + "MB";
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		ApplicationClass app = (ApplicationClass)getApplicationContext();
		app.loadOption(getApplicationContext());
	}
}
