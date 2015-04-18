package com.tao.lightning_of_dark;

import twitter4j.TwitterException;
import twitter4j.User;

import com.loopj.android.image.SmartImageView;
import com.tao.lightning_of_dark.R;

import UserPageFragment.UserPageFragmentPagerAdapter;
import UserPageFragment._0_detail;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserPage extends FragmentActivity {
	
	public static User target;
	SmartImageView banner, UserIcon;
	TextView Name, ScreenName;
	ImageView protect;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userpage);
		
		ViewPager viewPager = (ViewPager)findViewById(R.id.Userpager);
		viewPager.setAdapter(new UserPageFragmentPagerAdapter(getSupportFragmentManager()));
		viewPager.setOffscreenPageLimit(5);
		
		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.userPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		banner = (SmartImageView)findViewById(R.id.banner);
		UserIcon = (SmartImageView)findViewById(R.id.UserIcon);
		Name = (TextView)findViewById(R.id.UserName);
		ScreenName = (TextView)findViewById(R.id.UserScreenName);
		protect = (ImageView)findViewById(R.id.UserPage_protected);
		protect.setVisibility(View.GONE);
		
		final String u = getIntent().getStringExtra("userScreenName");
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					target = MainActivity.twitter.showUser(u);
					return true;
				} catch (TwitterException e) {
					return false;
				}
			}
			protected void onPostExecute(Boolean result) {
				if(result)
					functions();
				else
					new ShowToast("ユーザー情報を取得できませんでした", UserPage.this);
			}
		};
		task.execute();
	}
	
	public void functions(){
		getActionBar().setTitle(target.getName());
		if(target.isProtected())
			protect.setVisibility(View.VISIBLE);
		UserIcon.setImageUrl(target.getProfileImageURL());
		banner.setImageUrl(target.getProfileBannerURL());
		
		Name.setText(target.getName());
		ScreenName.setText("@" + target.getScreenName());
		
		new _0_detail().setText();
	}
	
	public void click_icon(View v){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(target.getProfileImageURL())));
	}
	public void click_banner(View v){
		if(target.getProfileBannerURL() != null)
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(target.getProfileBannerURL())));
	}
}
