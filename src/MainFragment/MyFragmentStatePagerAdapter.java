package MainFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
	
	SharedPreferences pref;
	boolean showList;

	public MyFragmentStatePagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		showList = pref.getBoolean("showList", false);
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 0:
			return new Fragment_mention();
			
		case 1:
			return new Fragment_home();
		
		case 2:
			if(showList)
				return new Fragment_List();
		}
		return null;
	}

	@Override
	public int getCount() {
		if(showList)
			return 3;
		else
			return 2;
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		switch(position){
		case 0:
			return "Mention";
			
		case 1: default:
			return "Home";
			
		case 2:
			if(pref.getString("SelectListName", null) != null)
				return pref.getString("SelectListName", null);
			else
				return "List";
		}
	}

}