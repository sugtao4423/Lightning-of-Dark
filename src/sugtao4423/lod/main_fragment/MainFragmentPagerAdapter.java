package sugtao4423.lod.main_fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import sugtao4423.lod.App;
import sugtao4423.lod.dataclass.TwitterList;

public class MainFragmentPagerAdapter extends FragmentStatePagerAdapter{

	private Context context;
	private App app;

	private Fragment_mention fragmentMention;
	private Fragment_home fragmentHome;

	public MainFragmentPagerAdapter(FragmentManager fm, Context context){
		super(fm);
		this.context = context;
		this.app = (App)context.getApplicationContext();

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
		default:
			return new Fragment_List(i - 2);
		}
	}

	@Override
	public int getCount(){
		return app.getLists(context).length + 2;
	}

	@Override
	public CharSequence getPageTitle(int position){
		TwitterList[] lists = app.getLists(context);
		switch(position){
		case 0:
			return "Mention";
		case 1:
			return "Home - Lv." + app.getLevel().getLevel();
		default:
			return lists[position - 2].getListName();
		}
	}

	public Fragment_mention getFragmentMention(){
		return fragmentMention;
	}

	public Fragment_home getFragmentHome(){
		return fragmentHome;
	}
}