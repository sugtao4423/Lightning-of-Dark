package sugtao4423.lod.swipe_image_viewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ImagePagerAdapter extends FragmentPagerAdapter{

    public static final String BUNDLE_KEY_URL = "url";

    private String[] urls;
    private ImageFragment[] fragments;

    public ImagePagerAdapter(FragmentManager fm, String[] urls){
        super(fm);
        this.urls = urls;
        fragments = new ImageFragment[urls.length];
        for(int i = 0; i < urls.length; i++){
            fragments[i] = new ImageFragment();
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_KEY_URL, urls[i]);
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
        return (position + 1) + "/" + getCount();
    }

}
