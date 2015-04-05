package com.tao.lightning_of_dark;

import com.tao.lightning_of_dark.R;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Preference extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content,  new MyPreferencesFragment()).commit();
		ActionBar actionbar = getActionBar();
		actionbar.setHomeButtonEnabled(true);
	}
	
	public static class MyPreferencesFragment extends PreferenceFragment {
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);
			
			android.preference.Preference optionMenu = findPreference("OptionMenu");
			android.preference.Preference ListSetting = findPreference("ListSetting");
			
			optionMenu.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(
						android.preference.Preference preference) {
					Intent intent = new Intent(getActivity(), Preference_OptionMenu.class);
					startActivity(intent);
					return false;
				}
			});
			ListSetting.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(
						android.preference.Preference preference) {
					Intent intent = new Intent(getActivity(), Preference_List.class);
					startActivity(intent);
					return false;
				}
			});
		}
	}
}
