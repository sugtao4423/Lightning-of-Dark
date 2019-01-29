package sugtao4423.lod.userpage_fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class UserPageFragmentPagerAdapter extends FragmentStatePagerAdapter{

    private _0_detail detail;
    private _1_Tweet tweet;
    private _2_favorites favorites;
    private _3_follow follow;
    private _4_follower follower;

    public UserPageFragmentPagerAdapter(FragmentManager fm){
        super(fm);
        detail = new _0_detail();
        tweet = new _1_Tweet();
        favorites = new _2_favorites();
        follow = new _3_follow();
        follower = new _4_follower();
    }

    @Override
    public Fragment getItem(int i){
        switch(i){
            case 0:
                return detail;
            case 1:
                return tweet;
            case 2:
                return favorites;
            case 3:
                return follow;
            case 4:
                return follower;
            default:
                return null;
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
                return "Detail";
            case 1:
                return "Tweet";
            case 2:
                return "favorites";
            case 3:
                return "follow";
            case 4:
                return "follower";
            default:
                return null;
        }
    }

}
