package com.tao.lightning_of_dark;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import com.loopj.android.image.SmartImageView;
import com.tao.lightning_of_dark.R;

import UserPageFragment.UserPageFragmentPagerAdapter;
import UserPageFragment._0_detail;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class UserPage extends FragmentActivity {
	
	static Twitter twitter;
	public static User target;
	static SmartImageView banner, UserIcon;
	static TextView Name, ScreenName;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userpage);
		
		ViewPager viewPager = (ViewPager)findViewById(R.id.Userpager);
		viewPager.setAdapter(new UserPageFragmentPagerAdapter(getSupportFragmentManager()));
		viewPager.setOffscreenPageLimit(5);
		
		twitter = MainActivity.twitter;
		banner = (SmartImageView)findViewById(R.id.banner);
		UserIcon = (SmartImageView)findViewById(R.id.UserIcon);
		Name = (TextView)findViewById(R.id.UserName);
		ScreenName = (TextView)findViewById(R.id.UserScreenName);
		
		final String u = getIntent().getStringExtra("userScreenName");
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					target = twitter.showUser(u);
					return true;
				} catch (TwitterException e) {
					return false;
				}
			}
			protected void onPostExecute(Boolean result) {
				if(result)
					functions();
				else
					showToast("ユーザー情報を取得できませんでした");
			}
		};
		task.execute();
	}
	
	public void functions(){
		getActionBar().setTitle(target.getName());
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
	
	public void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
}
