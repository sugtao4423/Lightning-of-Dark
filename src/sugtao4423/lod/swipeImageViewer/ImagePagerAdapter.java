package sugtao4423.lod.swipeImageViewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ImagePagerAdapter extends FragmentPagerAdapter{

	private String[] urls;
	private ImageFragment[] fragments;

	public ImagePagerAdapter(FragmentManager fm, String[] urls){
		super(fm);
		this.urls = urls;
		fragments = new ImageFragment[urls.length];
		for(int i = 0; i < urls.length; i++){
			fragments[i] = new ImageFragment();
			Bundle bundle = new Bundle();
			bundle.putString("url", urls[i]);
			fragments[i].setArguments(bundle);
		}
	}

	@Override
	public Fragment getItem(int i){
		return fragments[i];
	}

	@Override
	public int getCount(){
		return urls.length;
	}

	@Override
	public CharSequence getPageTitle(int position){
		return String.valueOf(position + 1) + "/" + String.valueOf(getCount());
	}
}
