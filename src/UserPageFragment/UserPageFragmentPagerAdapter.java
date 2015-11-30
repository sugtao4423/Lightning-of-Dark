package UserPageFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class UserPageFragmentPagerAdapter extends FragmentStatePagerAdapter{

	public UserPageFragmentPagerAdapter(FragmentManager fm){
		super(fm);
	}

	@Override
	public Fragment getItem(int i){
		switch(i){
		case 0:
		default:
			return new _0_detail();
		case 1:
			return new _1_Tweet();
		case 2:
			return new _2_favorites();
		case 3:
			return new _3_follow();
		case 4:
			return new _4_follower();
		}
	}

	@Override
	public int getCount(){
		return 5;
	}

	@Override
	public CharSequence getPageTitle(int position){
		switch(position){
		case 0:
		default:
			return "Detail";
		case 1:
			return "Tweet";
		case 2:
			return "favorites";
		case 3:
			return "follow";
		case 4:
			return "follower";
		}
	}

}
