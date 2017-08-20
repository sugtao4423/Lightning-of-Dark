package sugtao4423.lod.userpage_fragment;

import twitter4j.TwitterException;
import twitter4j.User;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import sugtao4423.lod.ApplicationClass;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;

public class UserPage extends FragmentActivity{

	public static final String INTENT_EXTRA_KEY_USER_OBJECT = "userObject";
	public static final String INTENT_EXTRA_KEY_USER_SCREEN_NAME = "userScreenName";

	private User target;
	private UserPageFragmentPagerAdapter adapter;
	private ApplicationClass appClass;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userpage);

		adapter = new UserPageFragmentPagerAdapter(getSupportFragmentManager());
		ViewPager viewPager = (ViewPager)findViewById(R.id.Userpager);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(5);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.userPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor(getString(R.color.pagerTabIndicator)));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		appClass = (ApplicationClass)UserPage.this.getApplicationContext();

		target = (User)getIntent().getSerializableExtra(INTENT_EXTRA_KEY_USER_OBJECT);
		if(target != null){
			setTargetUser();
			return;
		}

		new AsyncTask<Void, Void, User>(){
			@Override
			protected User doInBackground(Void... params){
				try{
					return appClass.getTwitter().showUser(getIntent().getStringExtra(INTENT_EXTRA_KEY_USER_SCREEN_NAME));
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(User result){
				if(result != null){
					target = result;
					setTargetUser();
					((_0_detail)(adapter.getItem(0))).setText();
				}else{
					new ShowToast(R.string.error_getUserDetail, UserPage.this, 0);
					finish();
				}
			}
		}.execute();
	}

	public void setTargetUser(){
		getActionBar().setTitle(target.getName());
		((_0_detail)(adapter.getItem(0))).setTargetUser(target);
		((_1_Tweet)(adapter.getItem(1))).setTargetUser(target);
		((_2_favorites)(adapter.getItem(2))).setTargetUser(target);
		((_3_follow)(adapter.getItem(3))).setTargetUser(target);
		((_4_follower)(adapter.getItem(4))).setTargetUser(target);
	}

}