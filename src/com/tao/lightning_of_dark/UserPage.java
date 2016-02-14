package com.tao.lightning_of_dark;

import twitter4j.TwitterException;
import twitter4j.User;

import com.tao.lightning_of_dark.R;

import UserPageFragment.UserPageFragmentPagerAdapter;
import UserPageFragment._0_detail;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

public class UserPage extends FragmentActivity{

	private User target;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userpage);

		ViewPager viewPager = (ViewPager)findViewById(R.id.Userpager);
		viewPager.setAdapter(new UserPageFragmentPagerAdapter(getSupportFragmentManager()));
		viewPager.setOffscreenPageLimit(5);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.userPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		final String u = getIntent().getStringExtra("userScreenName");
		((ApplicationClass)UserPage.this.getApplicationContext()).setTargetScreenName(u);

		new AsyncTask<Void, Void, User>(){
			@Override
			protected User doInBackground(Void... params){
				try{
					target = ((ApplicationClass)UserPage.this.getApplicationContext()).getTwitter().showUser(u);
					return target;
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(User result){
				if(result != null) {
					((ApplicationClass)UserPage.this.getApplicationContext()).setTarget(result);
					getActionBar().setTitle(result.getName());
					new _0_detail().setText(UserPage.this);
				}else{
					new ShowToast("ユーザー情報を取得できませんでした", UserPage.this, 0);
					finish();
				}
			}
		}.execute();
	}

	public void resetUser(){
		((ApplicationClass)UserPage.this.getApplicationContext()).setTargetScreenName(target.getScreenName());
	}
}