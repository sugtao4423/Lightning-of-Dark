package MainFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

	public MyFragmentStatePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 1:
			return new Fragment_home();
		case 0:
			return new Fragment_mention();
		}
		return null;
	}

	@Override
	public int getCount() {
		return 2;
	}
	
	@Override
	public CharSequence getPageTitle(int position){
		switch(position){
		case 1:
			return "Home";
		case 0:
			return "Mention";
		default:
			return "Home";
		}
	}

}