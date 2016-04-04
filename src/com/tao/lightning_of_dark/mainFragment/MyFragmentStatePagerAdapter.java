package com.tao.lightning_of_dark.mainFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter{

	private SharedPreferences pref;
	private boolean showList;
	private int listCount;

	private Fragment_mention fragmentMention;
	private Fragment_home fragmentHome;

	public MyFragmentStatePagerAdapter(FragmentManager fm, Context context){
		super(fm);
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		showList = pref.getBoolean("showList", false);
		listCount = pref.getInt("SelectListCount", 0);

		fragmentMention = new Fragment_mention();
		fragmentHome = new Fragment_home();
	}

	@Override
	public Fragment getItem(int i){
		switch(i){
		case 0:
			return fragmentMention;

		case 1:
			return fragmentHome;

		}
		if(showList && i > 1) {
			return new Fragment_List(i - 2);
		}
		return null;
	}

	@Override
	public int getCount(){
		if(showList)
			return listCount + 2;
		else
			return 2;
	}

	@Override
	public CharSequence getPageTitle(int position){
		if(position > 1) {
			if(!pref.getString("SelectListNames", "").equals("")) {
				String[] names = pref.getString("SelectListNames", null).split(",", 0);

				return names[position - 2];
			}else{
				return "List";
			}
		}
		switch(position){
		case 0:
			return "Mention";

		case 1:
		default:
			return "Home";

		}
	}

	public Fragment_mention getFragmentMention(){
		return fragmentMention;
	}

	public Fragment_home getFragmentHome(){
		return fragmentHome;
	}
}