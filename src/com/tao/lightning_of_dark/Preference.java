package com.tao.lightning_of_dark;

import com.tao.lightning_of_dark.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Preference extends PreferenceActivity{

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

			android.preference.Preference ListSetting = findPreference("ListSetting");

			ListSetting.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(android.preference.Preference preference){
					Intent intent = new Intent(getActivity(), Preference_List.class);
					startActivity(intent);
					return false;
				}
			});
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		ApplicationClass app = (ApplicationClass)getApplicationContext();
		app.loadOption(getApplicationContext());
	}
}
