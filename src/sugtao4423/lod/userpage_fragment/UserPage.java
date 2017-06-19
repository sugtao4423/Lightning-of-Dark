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

	private User target;
	private String userScreenName;
	private ApplicationClass appClass;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userpage);

		final UserPageFragmentPagerAdapter adapter = new UserPageFragmentPagerAdapter(getSupportFragmentManager());
		ViewPager viewPager = (ViewPager)findViewById(R.id.Userpager);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(5);

		PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.userPagerTabStrip);
		strip.setTabIndicatorColor(Color.parseColor("#33b5e5"));
		strip.setDrawFullUnderline(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		userScreenName = getIntent().getStringExtra("userScreenName");

		appClass = (ApplicationClass)UserPage.this.getApplicationContext();
		appClass.setTargetScreenName(userScreenName);

		new AsyncTask<Void, Void, User>(){
			@Override
			protected User doInBackground(Void... params){
				try{
					target = appClass.getTwitter().showUser(userScreenName);
					return target;
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(User result){
				if(result != null) {
					appClass.setTarget(result);
					getActionBar().setTitle(result.getName());
					((_0_detail)(adapter.getItem(0))).setText(UserPage.this);
				}else{
					new ShowToast("ユーザー情報を取得できませんでした", UserPage.this, 0);
					finish();
				}
			}
		}.execute();
	}

	public void resetUser(){
		appClass.setTarget(target);
		appClass.setTargetScreenName(userScreenName);
	}
}